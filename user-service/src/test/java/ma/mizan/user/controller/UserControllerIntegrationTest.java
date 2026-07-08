package ma.mizan.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import ma.mizan.common.security.InternalHeaders;
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
class UserControllerIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getOwnProfileWithoutTrustedHeaderReturns401() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void getOwnProfileLazilyCreatesDefaultProfile() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/users/me").header(InternalHeaders.USER_ID, userId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId)).andExpect(jsonPath("$.displayName").value("New User"))
				.andExpect(jsonPath("$.preferredLang").value("fr"));
	}

	@Test
	void updateLanguagePreferenceIsReflectedOnNextGet() throws Exception {
		String userId = UUID.randomUUID().toString();
		var updateBody = Map.of("displayName", "Fatima Z.", "preferredLang", "ary");

		mockMvc.perform(put("/api/v1/users/me").header(InternalHeaders.USER_ID, userId)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateBody)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.preferredLang").value("ary"));

		mockMvc.perform(get("/api/v1/users/me").header(InternalHeaders.USER_ID, userId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Fatima Z."))
				.andExpect(jsonPath("$.preferredLang").value("ary"));
	}

	@Test
	void updateWithInvalidLanguageIsRejected() throws Exception {
		String userId = UUID.randomUUID().toString();
		var updateBody = Map.of("displayName", "Someone", "preferredLang", "xx");

		mockMvc.perform(put("/api/v1/users/me").header(InternalHeaders.USER_ID, userId)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateBody)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
	}
}
