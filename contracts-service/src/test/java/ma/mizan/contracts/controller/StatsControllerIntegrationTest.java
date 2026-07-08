package ma.mizan.contracts.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.contracts.domain.AnalysisResult;
import ma.mizan.contracts.domain.ClauseFlag;
import ma.mizan.contracts.domain.Contract;
import ma.mizan.contracts.domain.ContractStatus;
import ma.mizan.contracts.domain.RiskLevel;
import ma.mizan.contracts.repository.ContractRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class StatsControllerIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Container
	static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
			.withServices(LocalStackContainer.Service.S3);

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("r2.endpoint-url", () -> localstack.getEndpoint().toString());
	}

	@Autowired
	private org.springframework.test.web.servlet.MockMvc mockMvc;

	@Autowired
	private ContractRepository contractRepository;

	@Test
	void nonAdminIsForbidden() throws Exception {
		mockMvc.perform(get("/api/v1/contracts/stats").header(InternalHeaders.USER_ROLE, "INDIVIDUAL"))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminSeesAggregateCounts() throws Exception {
		Contract complete = new Contract(UUID.randomUUID(), "a.pdf", "key-a");
		complete.setStatus(ContractStatus.COMPLETE);
		var analysis = new AnalysisResult(complete, "fr", "summary", null);
		analysis.addClauseFlag(new ClauseFlag("clause", RiskLevel.HIGH, "explanation", null));
		complete.attachAnalysisResult(analysis);
		contractRepository.save(complete);

		Contract failed = new Contract(UUID.randomUUID(), "b.pdf", "key-b");
		failed.setStatus(ContractStatus.FAILED);
		contractRepository.save(failed);

		mockMvc.perform(get("/api/v1/contracts/stats").header(InternalHeaders.USER_ROLE, "ADMIN"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.byStatus.COMPLETE").value(1))
				.andExpect(jsonPath("$.byStatus.FAILED").value(1)).andExpect(jsonPath("$.byRiskLevel.HIGH").value(1));
	}
}
