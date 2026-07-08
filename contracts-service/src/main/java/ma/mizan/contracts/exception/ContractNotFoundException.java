package ma.mizan.contracts.exception;

import java.util.UUID;

public class ContractNotFoundException extends RuntimeException {

	public ContractNotFoundException(UUID contractId) {
		super("No contract found with id " + contractId);
	}
}
