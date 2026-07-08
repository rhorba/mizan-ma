package ma.mizan.aianalysis.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.mizan.aianalysis.exception.AnthropicApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AnthropicClient {

	private final RestClient anthropicRestClient;
	private final ObjectMapper objectMapper;
	private final String model;

	public AnthropicClient(RestClient anthropicRestClient, ObjectMapper objectMapper,
			@Value("${anthropic.model}") String model) {
		this.anthropicRestClient = anthropicRestClient;
		this.objectMapper = objectMapper;
		this.model = model;
	}

	/**
	 * Sends a single-turn message to the Claude Messages API and returns the raw
	 * JSON response body (persisted as-is by contracts-service for audit/debug).
	 */
	public String sendMessage(String systemPrompt, String userMessage) {
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("model", model);
		requestBody.put("max_tokens", 4096);
		requestBody.put("system", systemPrompt);
		ArrayNode messages = requestBody.putArray("messages");
		ObjectNode userTurn = messages.addObject();
		userTurn.put("role", "user");
		userTurn.put("content", userMessage);

		try {
			return anthropicRestClient.post().uri("/v1/messages").body(requestBody).retrieve().body(String.class);
		} catch (RestClientResponseException e) {
			throw new AnthropicApiException(
					"Anthropic API returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
		} catch (Exception e) {
			throw new AnthropicApiException("Failed to call Anthropic API", e);
		}
	}

	public String extractText(String rawResponseJson) {
		try {
			JsonNode root = objectMapper.readTree(rawResponseJson);
			JsonNode content = root.path("content");
			if (!content.isArray() || content.isEmpty()) {
				throw new AnthropicApiException("Anthropic response had no content blocks");
			}
			return content.get(0).path("text").asText();
		} catch (AnthropicApiException e) {
			throw e;
		} catch (Exception e) {
			throw new AnthropicApiException("Failed to parse Anthropic response", e);
		}
	}
}
