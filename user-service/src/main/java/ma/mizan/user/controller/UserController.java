package ma.mizan.user.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.user.controller.dto.ProfileResponse;
import ma.mizan.user.controller.dto.UpdateProfileRequest;
import ma.mizan.user.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserProfileService userProfileService;

	public UserController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping("/me")
	public ProfileResponse getOwnProfile(@RequestHeader(InternalHeaders.USER_ID) UUID userId) {
		return userProfileService.getOwnProfile(userId);
	}

	@PutMapping("/me")
	public ProfileResponse updateOwnProfile(@RequestHeader(InternalHeaders.USER_ID) UUID userId,
			@Valid @RequestBody UpdateProfileRequest request) {
		return userProfileService.updateOwnProfile(userId, request);
	}
}
