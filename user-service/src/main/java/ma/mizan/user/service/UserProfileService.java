package ma.mizan.user.service;

import java.util.UUID;
import ma.mizan.user.controller.dto.ProfileResponse;
import ma.mizan.user.controller.dto.UpdateProfileRequest;
import ma.mizan.user.domain.UserProfile;
import ma.mizan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

	private static final String DEFAULT_DISPLAY_NAME = "New User";

	private final UserProfileRepository userProfileRepository;

	public UserProfileService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	@Transactional
	public ProfileResponse getOwnProfile(UUID userId) {
		return ProfileResponse.from(getOrCreate(userId));
	}

	@Transactional
	public ProfileResponse updateOwnProfile(UUID userId, UpdateProfileRequest request) {
		UserProfile profile = getOrCreate(userId);
		profile.update(request.displayName(), request.businessName(), request.preferredLang());
		return ProfileResponse.from(profile);
	}

	private UserProfile getOrCreate(UUID userId) {
		return userProfileRepository.findByUserId(userId)
				.orElseGet(() -> userProfileRepository.save(new UserProfile(userId, DEFAULT_DISPLAY_NAME)));
	}
}
