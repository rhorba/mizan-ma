package ma.mizan.user.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(@NotBlank String displayName, String businessName,
		@Pattern(regexp = "ar|fr|ary", message = "must be one of: ar, fr, ary") @NotBlank String preferredLang) {
}
