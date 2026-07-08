package ma.mizan.user.controller.dto;

import java.util.UUID;
import ma.mizan.user.domain.UserProfile;

public record ProfileResponse(UUID userId, String displayName, String businessName, String preferredLang) {

	public static ProfileResponse from(UserProfile profile) {
		return new ProfileResponse(profile.getUserId(), profile.getDisplayName(), profile.getBusinessName(),
				profile.getPreferredLang());
	}
}
