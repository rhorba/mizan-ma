package ma.mizan.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import ma.mizan.common.persistence.UuidEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class User extends UuidEntity {

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected User() {
	}

	public User(String email, String passwordHash, Role role) {
		super(UUID.randomUUID());
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Role getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void markEmailVerified() {
		this.emailVerified = true;
	}
}
