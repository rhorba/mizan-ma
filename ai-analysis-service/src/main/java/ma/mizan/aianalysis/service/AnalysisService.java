package ma.mizan.aianalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import ma.mizan.aianalysis.client.AnthropicClient;
import ma.mizan.aianalysis.controller.dto.AnalyzeRequest;
import ma.mizan.aianalysis.controller.dto.AnalyzeResponse;
import ma.mizan.aianalysis.controller.dto.ClauseFlagDto;
import ma.mizan.aianalysis.exception.AnthropicApiException;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

	private static final String SYSTEM_PROMPT = """
			You are a legal contract analysis assistant for Mizan.ma, helping non-lawyers in Morocco \
			understand contracts before they sign. Analyze the contract text the user provides and \
			respond with a summary, and a list of clauses worth flagging (unusual, risky, or unfavorable \
			terms), each with a risk level and a plain-language explanation.

			Respond ONLY with a single JSON object matching exactly this shape, with no markdown \
			formatting, no code fences, and no text outside the JSON:
			{
			  "summary": "<plain-language overview of the contract, 2-4 sentences>",
			  "clauseFlags": [
			    {
			      "clauseText": "<the exact or closely paraphrased clause text>",
			      "riskLevel": "<LOW, MEDIUM, or HIGH>",
			      "explanation": "<why this clause matters, in plain language>",
			      "suggestedCorrection": "<an alternative phrasing that would be fairer, or null if not applicable>"
			    }
			  ]
			}

			Write the summary, explanations, and suggested corrections in the language requested by the user.""";

	private final AnthropicClient anthropicClient;
	private final ObjectMapper objectMapper;

	public AnalysisService(AnthropicClient anthropicClient, ObjectMapper objectMapper) {
		this.anthropicClient = anthropicClient;
		this.objectMapper = objectMapper;
	}

	public AnalyzeResponse analyze(AnalyzeRequest request) {
		String userMessage = "Target language: %s\n\nContract text:\n%s".formatted(request.language(),
				request.contractText());

		String rawResponse = anthropicClient.sendMessage(SYSTEM_PROMPT, userMessage);
		String analysisJson = anthropicClient.extractText(rawResponse);

		return parseAnalysis(analysisJson, rawResponse);
	}

	private AnalyzeResponse parseAnalysis(String analysisJson, String rawResponse) {
		JsonNode analysis;
		try {
			analysis = objectMapper.readTree(analysisJson);
		} catch (Exception e) {
			throw new AnthropicApiException("Model response was not valid JSON: " + analysisJson, e);
		}

		String summary = analysis.path("summary").asText();
		List<ClauseFlagDto> clauseFlags = new ArrayList<>();
		for (JsonNode flag : analysis.path("clauseFlags")) {
			clauseFlags.add(new ClauseFlagDto(flag.path("clauseText").asText(), flag.path("riskLevel").asText(),
					flag.path("explanation").asText(),
					flag.hasNonNull("suggestedCorrection") ? flag.path("suggestedCorrection").asText() : null));
		}

		return new AnalyzeResponse(summary, clauseFlags, rawResponse);
	}
}
