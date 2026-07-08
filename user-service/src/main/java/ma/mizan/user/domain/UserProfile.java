package ma.mizan.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import ma.mizan.common.persistence.UuidEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends UuidEntity {

	@Column(name = "user_id", nullable = false, unique = true)
	private UUID userId;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "business_name")
	private String businessName;

	@Column(name = "preferred_lang", nullable = false)
	private String preferredLang = "fr";

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserProfile() {
	}

	public UserProfile(UUID userId, String displayName) {
		super(UUID.randomUUID());
		this.userId = userId;
		this.displayName = displayName;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getBusinessName() {
		return businessName;
	}

	public String getPreferredLang() {
		return preferredLang;
	}

	public void update(String displayName, String businessName, String preferredLang) {
		this.displayName = displayName;
		this.businessName = businessName;
		this.preferredLang = preferredLang;
	}
}
