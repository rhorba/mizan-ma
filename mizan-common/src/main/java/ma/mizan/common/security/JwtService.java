package ma.mizan.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

/**
 * Issues and parses the access-token JWT shared between auth-service (issuer)
 * and the Gateway (validator, ADR-3). Both must agree on the claims structure,
 * hence this lives in mizan-common rather than being duplicated per service
 * (ADR-2).
 */
public class JwtService {

	public static final String ROLE_CLAIM = "role";

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(String secret, long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateAccessToken(UUID userId, String role) {
		Instant now = Instant.now();
		return Jwts.builder().subject(userId.toString()).claim(ROLE_CLAIM, role).issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs))).signWith(key).compact();
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	/**
	 * @throws io.jsonwebtoken.JwtException
	 *             if the token is malformed, expired, or has an invalid signature.
	 */
	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}
}
