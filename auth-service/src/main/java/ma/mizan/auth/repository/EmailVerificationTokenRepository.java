package ma.mizan.auth.repository;

import java.util.Optional;
import java.util.UUID;
import ma.mizan.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

	Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

	Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
