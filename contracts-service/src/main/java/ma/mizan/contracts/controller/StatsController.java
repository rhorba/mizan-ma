package ma.mizan.contracts.controller;

import ma.mizan.common.security.InternalHeaders;
import ma.mizan.contracts.controller.dto.ContractStatsResponse;
import ma.mizan.contracts.exception.AdminAccessRequiredException;
import ma.mizan.contracts.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contracts")
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/stats")
	public ContractStatsResponse stats(@RequestHeader(InternalHeaders.USER_ROLE) String role) {
		if (!"ADMIN".equals(role)) {
			throw new AdminAccessRequiredException();
		}
		return statsService.getStats();
	}
}
