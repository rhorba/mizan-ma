package ma.mizan.contracts.controller;

import java.util.List;
import java.util.UUID;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.contracts.controller.dto.ContractDetailResponse;
import ma.mizan.contracts.controller.dto.ContractSummaryResponse;
import ma.mizan.contracts.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

	private final ContractService contractService;

	public ContractController(ContractService contractService) {
		this.contractService = contractService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ContractSummaryResponse upload(@RequestHeader(InternalHeaders.USER_ID) UUID userId,
			@RequestParam("file") MultipartFile file,
			@RequestParam(name = "language", defaultValue = "fr") String language) {
		return contractService.upload(userId, file, language);
	}

	@GetMapping
	public List<ContractSummaryResponse> list(@RequestHeader(InternalHeaders.USER_ID) UUID userId) {
		return contractService.listOwn(userId);
	}

	@GetMapping("/{id}")
	public ContractDetailResponse get(@RequestHeader(InternalHeaders.USER_ID) UUID userId,
			@PathVariable("id") UUID id) {
		return contractService.getOwn(userId, id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestHeader(InternalHeaders.USER_ID) UUID userId, @PathVariable("id") UUID id) {
		contractService.deleteOwn(userId, id);
	}
}
