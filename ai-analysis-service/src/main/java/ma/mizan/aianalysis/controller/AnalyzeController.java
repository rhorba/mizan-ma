package ma.mizan.aianalysis.controller;

import jakarta.validation.Valid;
import ma.mizan.aianalysis.controller.dto.AnalyzeRequest;
import ma.mizan.aianalysis.controller.dto.AnalyzeResponse;
import ma.mizan.aianalysis.exception.UnauthorizedInternalCallException;
import ma.mizan.aianalysis.service.AnalysisService;
import ma.mizan.common.security.InternalHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Not gateway-routed (architecture-mizan.md §5) — reachable only from inside
 * the internal network.
 */
@RestController
@RequestMapping("/internal/v1")
public class AnalyzeController {

	private final AnalysisService analysisService;
	private final String internalToken;

	public AnalyzeController(AnalysisService analysisService,
			@Value("${internal.service-token}") String internalToken) {
		this.analysisService = analysisService;
		this.internalToken = internalToken;
	}

	@PostMapping("/analyze")
	public AnalyzeResponse analyze(@RequestHeader(InternalHeaders.INTERNAL_TOKEN) String token,
			@Valid @RequestBody AnalyzeRequest request) {
		if (!internalToken.equals(token)) {
			throw new UnauthorizedInternalCallException();
		}
		return analysisService.analyze(request);
	}
}
