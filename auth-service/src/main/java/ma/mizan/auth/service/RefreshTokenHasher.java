package ma.mizan.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Refresh tokens carry their own entropy (SecureRandom), so a fast SHA-256
 * digest is sufficient for at-rest storage (security-mizan.md §3) — unlike
 * passwords, they don't need bcrypt.
 */
@Component
class RefreshTokenHasher {

	private final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

	String generateRawToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	String hash(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}
}
