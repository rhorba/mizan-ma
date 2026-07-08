package ma.mizan.contracts.controller.dto;

import ma.mizan.contracts.domain.ClauseFlag;

public record ClauseFlagResponse(String clauseText, String riskLevel, String explanation, String suggestedCorrection) {

	public static ClauseFlagResponse from(ClauseFlag flag) {
		return new ClauseFlagResponse(flag.getClauseText(), flag.getRiskLevel().name(), flag.getExplanation(),
				flag.getSuggestedCorrection());
	}
}
