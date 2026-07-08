package ma.mizan.contracts.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ma.mizan.contracts.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

	List<Contract> findByUserIdOrderByCreatedAtDesc(UUID userId);

	@Query("select c from Contract c " + "left join fetch c.analysisResult ar " + "left join fetch ar.clauseFlags "
			+ "where c.id = :id")
	Optional<Contract> findByIdWithAnalysis(@Param("id") UUID id);
}
