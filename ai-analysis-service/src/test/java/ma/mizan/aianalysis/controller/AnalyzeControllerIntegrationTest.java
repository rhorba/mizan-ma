package ma.mizan.aianalysis.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyzeControllerIntegrationTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort()).build();

	@DynamicPropertySource
	static void anthropicBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("anthropic.base-url", wireMock::baseUrl);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static final String CLAUDE_RESPONSE_WRAPPER = """
			{
			  "id": "msg_test",
			  "type": "message",
			  "role": "assistant",
			  "content": [{"type": "text", "text": %s}],
			  "model": "claude-sonnet-4-6",
			  "stop_reason": "end_turn"
			}""";

	@Test
	void analyzeReturnsParsedSummaryAndFlags() throws Exception {
		String analysisJson = """
				{
				  "summary": "This is a standard lease agreement with one concerning clause.",
				  "clauseFlags": [
				    {
				      "clauseText": "Tenant waives all rights to dispute rent increases.",
				      "riskLevel": "HIGH",
				      "explanation": "This removes your legal right to contest unfair rent increases.",
				      "suggestedCorrection": "Rent increases are capped at 5% per year."
				    }
				  ]
				}""";
		String claudeBody = CLAUDE_RESPONSE_WRAPPER.formatted(objectMapper.writeValueAsString(analysisJson));

		wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/v1/messages"))
				.willReturn(aResponse().withStatus(200).withBody(claudeBody)));

		mockMvc.perform(post("/internal/v1/analyze").header("X-Internal-Token", "test-only-internal-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(Map.of("contractText", "Some lease contract text", "language", "fr"))))
				.andExpect(status().isOk())
				.andExpect(
						jsonPath("$.summary").value("This is a standard lease agreement with one concerning clause."))
				.andExpect(jsonPath("$.clauseFlags[0].riskLevel").value("HIGH"))
				.andExpect(jsonPath("$.rawResponse").isNotEmpty());
	}

	@Test
	void analyzeWithWrongTokenReturns403() throws Exception {
		mockMvc.perform(post("/internal/v1/analyze").header("X-Internal-Token", "wrong-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("contractText", "text", "language", "fr"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void analyzeWithoutTokenReturns400() throws Exception {
		mockMvc.perform(post("/internal/v1/analyze").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("contractText", "text", "language", "fr"))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void anthropicUpstreamFailureReturns502() throws Exception {
		wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/v1/messages"))
				.willReturn(aResponse().withStatus(500)));

		mockMvc.perform(post("/internal/v1/analyze").header("X-Internal-Token", "test-only-internal-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("contractText", "text", "language", "fr"))))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.error.code").value("ANALYSIS_UPSTREAM_ERROR"));
	}
}
