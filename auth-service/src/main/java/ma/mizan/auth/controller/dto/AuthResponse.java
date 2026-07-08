package ma.mizan.auth.controller.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInMs) {

	public AuthResponse(String accessToken, String refreshToken, long expiresInMs) {
		this(accessToken, refreshToken, "Bearer", expiresInMs);
	}
}
