package ma.mizan.auth.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import ma.mizan.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void registerThenLoginThenRefreshThenLogout() throws Exception {
		String email = "alice@example.com";

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", email, "password", "correct-horse-battery", "role", "INDIVIDUAL"))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.role").value("INDIVIDUAL"));

		// Verification-token delivery (email) is exercised separately in
		// EmailVerificationIntegrationTest;
		// this test is about the login/refresh/logout token lifecycle, so mark verified
		// directly.
		userRepository.findByEmail(email).ifPresent(user -> {
			user.markEmailVerified();
			userRepository.save(user);
		});

		String loginBody = mockMvc
				.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(
						objectMapper.writeValueAsString(Map.of("email", email, "password", "correct-horse-battery"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.refreshToken", notNullValue())).andReturn().getResponse().getContentAsString();

		String refreshToken = objectMapper.readTree(loginBody).get("refreshToken").asText();

		String refreshBody = mockMvc
				.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken", notNullValue())).andReturn()
				.getResponse().getContentAsString();
		String rotatedRefreshToken = objectMapper.readTree(refreshBody).get("refreshToken").asText();

		// The original refresh token was rotated out on use — replaying it must fail.
		mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", rotatedRefreshToken))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", rotatedRefreshToken))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registerRejectsDuplicateEmail() throws Exception {
		String email = "bob@example.com";
		Map<String, String> payload = Map.of("email", email, "password", "correct-horse-battery", "role", "BUSINESS");

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload))).andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void registerRejectsShortPassword() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", "carol@example.com", "password", "short", "role", "INDIVIDUAL"))))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
	}

	@Test
	void loginRejectsUnverifiedEmail() throws Exception {
		String email = "erin@example.com";
		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", email, "password", "correct-horse-battery", "role", "INDIVIDUAL"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", "correct-horse-battery"))))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		String email = "dave@example.com";
		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", email, "password", "correct-horse-battery", "role", "INDIVIDUAL"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", "wrong-password"))))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
	}
}
