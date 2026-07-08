package ma.mizan.contracts.service;

import java.util.LinkedHashMap;
import java.util.Map;
import ma.mizan.contracts.controller.dto.ContractStatsResponse;
import ma.mizan.contracts.repository.ClauseFlagRepository;
import ma.mizan.contracts.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

	private final ContractRepository contractRepository;
	private final ClauseFlagRepository clauseFlagRepository;

	public StatsService(ContractRepository contractRepository, ClauseFlagRepository clauseFlagRepository) {
		this.contractRepository = contractRepository;
		this.clauseFlagRepository = clauseFlagRepository;
	}

	@Transactional(readOnly = true)
	public ContractStatsResponse getStats() {
		Map<String, Long> byStatus = new LinkedHashMap<>();
		for (var row : contractRepository.countByStatus()) {
			byStatus.put(row.getStatus().name(), row.getCount());
		}

		Map<String, Long> byRiskLevel = new LinkedHashMap<>();
		for (var row : clauseFlagRepository.countByRiskLevel()) {
			byRiskLevel.put(row.getRiskLevel().name(), row.getCount());
		}

		return new ContractStatsResponse(byStatus, byRiskLevel);
	}
}
