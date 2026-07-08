package ma.mizan.contracts.client;

import java.util.Map;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.contracts.client.dto.AnalysisOutcome;
import ma.mizan.contracts.exception.AiAnalysisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiAnalysisClient {

	private final RestClient aiAnalysisRestClient;
	private final String internalToken;

	public AiAnalysisClient(RestClient aiAnalysisRestClient, @Value("${internal.service-token}") String internalToken) {
		this.aiAnalysisRestClient = aiAnalysisRestClient;
		this.internalToken = internalToken;
	}

	public AnalysisOutcome analyze(String contractText, String language) {
		try {
			return aiAnalysisRestClient.post().uri("/internal/v1/analyze")
					.header(InternalHeaders.INTERNAL_TOKEN, internalToken)
					.body(Map.of("contractText", contractText, "language", language)).retrieve()
					.body(AnalysisOutcome.class);
		} catch (Exception e) {
			throw new AiAnalysisException("ai-analysis-service call failed: " + e.getMessage(), e);
		}
	}
}
