package ma.mizan.aianalysis.controller.dto;

import java.util.List;

public record AnalyzeResponse(String summary, List<ClauseFlagDto> clauseFlags, String rawResponse) {
}
