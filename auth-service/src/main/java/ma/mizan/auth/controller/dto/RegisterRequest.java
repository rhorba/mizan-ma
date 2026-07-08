package ma.mizan.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@Email @NotNull String email,
		@Size(min = 10, message = "must be at least 10 characters") @NotNull String password,
		@NotNull RegistrableRole role) {
}
