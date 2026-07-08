package ma.mizan.contracts.controller.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ma.mizan.contracts.domain.AnalysisResult;
import ma.mizan.contracts.domain.Contract;

public record ContractDetailResponse(UUID id, String fileName, String status, Instant createdAt, String summary,
		List<ClauseFlagResponse> clauseFlags) {

	public static ContractDetailResponse from(Contract contract) {
		AnalysisResult analysisResult = contract.getAnalysisResult();
		String summary = analysisResult != null ? analysisResult.getSummary() : null;
		List<ClauseFlagResponse> flags = analysisResult != null
				? analysisResult.getClauseFlags().stream().map(ClauseFlagResponse::from).toList()
				: List.of();
		return new ContractDetailResponse(contract.getId(), contract.getFileName(), contract.getStatus().name(),
				contract.getCreatedAt(), summary, flags);
	}
}
