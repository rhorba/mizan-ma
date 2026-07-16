package ma.mizan.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResendVerificationRequest(@Email @NotNull String email) {
}
