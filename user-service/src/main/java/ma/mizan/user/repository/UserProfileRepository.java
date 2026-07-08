package ma.mizan.user.repository;

import java.util.Optional;
import java.util.UUID;
import ma.mizan.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

	Optional<UserProfile> findByUserId(UUID userId);
}
