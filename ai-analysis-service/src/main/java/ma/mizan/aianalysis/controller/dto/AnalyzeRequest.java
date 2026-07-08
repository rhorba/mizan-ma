package ma.mizan.aianalysis.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeRequest(@NotBlank String contractText, @NotBlank String language) {
}
