package ma.mizan.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Lives in ma.mizan.auth.service (not .controller) because it needs a real
 * subclass of the package-private MailService to capture the raw verification
 * token — the token is deliberately never returned by the API itself, only ever
 * emailed. Uses a hand-written test double (@TestConfiguration bean override)
 * rather than a Mockito class-mock, matching the approach used elsewhere in
 * this codebase to avoid depending on Mockito's inline mock maker.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class EmailVerificationIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@TestConfiguration
	static class FakeMailConfig {

		@Bean
		@Primary
		MailService mailService() {
			return new FakeMailService();
		}
	}

	static class FakeMailService extends MailService {

		String lastEmail;
		String lastToken;

		FakeMailService() {
			super(null, "test@mizan.ma", "http://localhost:4200", false);
		}

		@Override
		void sendVerificationEmail(String toEmail, String rawToken) {
			this.lastEmail = toEmail;
			this.lastToken = rawToken;
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MailService mailService;

	private String registerAndCaptureToken(String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", email, "password", "correct-horse-battery", "role", "INDIVIDUAL"))))
				.andExpect(status().isCreated());

		var fake = (FakeMailService) mailService;
		assertEquals(email, fake.lastEmail);
		return fake.lastToken;
	}

	@Test
	void verifyingWithTheEmailedTokenUnblocksLogin() throws Exception {
		String email = "frank@example.com";
		String rawToken = registerAndCaptureToken(email);

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", "correct-horse-battery"))))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("token", rawToken)))).andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", "correct-horse-battery"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());
	}

	@Test
	void verifyRejectsAGarbageToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("token", "not-a-real-token"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_VERIFICATION_TOKEN"));
	}

	@Test
	void verifyRejectsAnAlreadyUsedToken() throws Exception {
		String email = "grace@example.com";
		String rawToken = registerAndCaptureToken(email);

		mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("token", rawToken)))).andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("token", rawToken)))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_VERIFICATION_TOKEN"));
	}

	@Test
	void resendImmediatelyAfterRegisterHitsTheCooldown() throws Exception {
		String email = "heidi@example.com";
		registerAndCaptureToken(email);

		mockMvc.perform(post("/api/v1/auth/resend-verification").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email))))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.error.code").value("VERIFICATION_RESEND_TOO_SOON"));
	}

	@Test
	void resendForAnUnknownEmailIsSilentlyANoOp() throws Exception {
		mockMvc.perform(post("/api/v1/auth/resend-verification").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", "nobody@example.com"))))
				.andExpect(status().isNoContent());
	}
}
