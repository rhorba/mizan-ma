package ma.mizan.auth.controller;

import jakarta.validation.Valid;
import ma.mizan.auth.controller.dto.AuthResponse;
import ma.mizan.auth.controller.dto.LoginRequest;
import ma.mizan.auth.controller.dto.RefreshRequest;
import ma.mizan.auth.controller.dto.RegisterRequest;
import ma.mizan.auth.controller.dto.RegisterResponse;
import ma.mizan.auth.controller.dto.ResendVerificationRequest;
import ma.mizan.auth.controller.dto.VerifyEmailRequest;
import ma.mizan.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody RefreshRequest request) {
		authService.logout(request.refreshToken());
	}

	@PostMapping("/verify-email")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		authService.verifyEmail(request.token());
	}

	@PostMapping("/resend-verification")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
		authService.resendVerification(request.email());
	}
}
