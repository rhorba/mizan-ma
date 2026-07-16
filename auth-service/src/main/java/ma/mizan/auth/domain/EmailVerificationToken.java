package ma.mizan.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import ma.mizan.common.persistence.UuidEntity;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken extends UuidEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "token_hash", nullable = false)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean used = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected EmailVerificationToken() {
	}

	public EmailVerificationToken(UUID userId, String tokenHash, Instant expiresAt) {
		super(UUID.randomUUID());
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isUsed() {
		return used;
	}

	public void markUsed() {
		this.used = true;
	}

	public boolean isUsable() {
		return !used && expiresAt.isAfter(Instant.now());
	}
}
