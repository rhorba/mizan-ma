package ma.mizan.common.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Every entity in this codebase assigns its own UUID id in its constructor
 * (never {@code @GeneratedValue}), so Spring Data's default {@code isNew()}
 * check — which just tests whether the id is null — always says "not new" and
 * routes every {@code save()} through {@code merge()} instead of
 * {@code persist()}. {@code merge()} returns a different managed instance than
 * the one passed in, so any further mutation of the original in-memory object
 * (e.g. setting status after the initial save) is silently lost — it never
 * reaches the entity Hibernate actually flushes. Implementing
 * {@link Persistable} with an explicit transient "new" flag fixes this at the
 * source for every entity that extends this class.
 */
@MappedSuperclass
public abstract class UuidEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Transient
	private boolean isNew = true;

	protected UuidEntity() {
	}

	protected UuidEntity(UUID id) {
		this.id = id;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostPersist
	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}
}
