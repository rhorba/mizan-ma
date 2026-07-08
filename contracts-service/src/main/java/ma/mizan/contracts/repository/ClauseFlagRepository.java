package ma.mizan.contracts.repository;

import java.util.List;
import java.util.UUID;
import ma.mizan.contracts.domain.ClauseFlag;
import ma.mizan.contracts.domain.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClauseFlagRepository extends JpaRepository<ClauseFlag, UUID> {

	@Query("select f.riskLevel as riskLevel, count(f) as count from ClauseFlag f group by f.riskLevel")
	List<RiskLevelCount> countByRiskLevel();

	interface RiskLevelCount {
		RiskLevel getRiskLevel();

		long getCount();
	}
}
