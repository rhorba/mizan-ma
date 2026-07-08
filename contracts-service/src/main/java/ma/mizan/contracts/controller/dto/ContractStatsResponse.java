package ma.mizan.contracts.controller.dto;

import java.util.Map;

public record ContractStatsResponse(Map<String, Long> byStatus, Map<String, Long> byRiskLevel) {
}
