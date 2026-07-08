package ma.mizan.contracts.client.dto;

import java.util.List;

public record AnalysisOutcome(String summary, List<ClauseFlagResult> clauseFlags, String rawResponse) {
}
