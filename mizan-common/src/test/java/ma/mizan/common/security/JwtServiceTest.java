package ma.mizan.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private static final String SECRET = "test-secret-at-least-256-bits-long-for-hs256-signing";

	@Test
	void generatedTokenParsesBackToSameSubjectAndRole() {
		var jwtService = new JwtService(SECRET, 3_600_000);
		var userId = UUID.randomUUID();

		var token = jwtService.generateAccessToken(userId, "INDIVIDUAL");
		var claims = jwtService.parseClaims(token);

		assertEquals(userId.toString(), claims.getSubject());
		assertEquals("INDIVIDUAL", claims.get(JwtService.ROLE_CLAIM, String.class));
	}

	@Test
	void expiredTokenFailsToParse() {
		var jwtService = new JwtService(SECRET, -1_000);
		var token = jwtService.generateAccessToken(UUID.randomUUID(), "BUSINESS");

		assertThrows(ExpiredJwtException.class, () -> jwtService.parseClaims(token));
	}

	@Test
	void tokenSignedWithDifferentSecretFailsToParse() {
		var issuer = new JwtService(SECRET, 3_600_000);
		var verifier = new JwtService("a-completely-different-secret-value-of-sufficient-length", 3_600_000);
		var token = issuer.generateAccessToken(UUID.randomUUID(), "ADMIN");

		assertThrows(JwtException.class, () -> verifier.parseClaims(token));
	}
}
