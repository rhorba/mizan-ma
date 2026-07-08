package ma.mizan.contracts.controller.dto;

import java.time.Instant;
import java.util.UUID;
import ma.mizan.contracts.domain.Contract;

public record ContractSummaryResponse(UUID id, String fileName, String status, Instant createdAt) {

	public static ContractSummaryResponse from(Contract contract) {
		return new ContractSummaryResponse(contract.getId(), contract.getFileName(), contract.getStatus().name(),
				contract.getCreatedAt());
	}
}
