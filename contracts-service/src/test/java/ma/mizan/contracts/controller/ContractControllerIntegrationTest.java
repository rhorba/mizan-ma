package ma.mizan.contracts.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import ma.mizan.common.security.InternalHeaders;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ContractControllerIntegrationTest {

	private static final String BUCKET_NAME = "test-contracts";

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Container
	static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
			.withServices(LocalStackContainer.Service.S3);

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("r2.endpoint-url", () -> localstack.getEndpoint().toString());
		registry.add("ai-analysis.service-url", wireMock::baseUrl);
	}

	@BeforeAll
	static void createBucket() {
		S3Client s3 = S3Client.builder().endpointOverride(localstack.getEndpoint())
				.region(Region.of(localstack.getRegion()))
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
				.forcePathStyle(true).build();
		s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static byte[] pdfWithText(String text) throws Exception {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
				stream.beginText();
				stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				stream.newLineAtOffset(50, 700);
				stream.showText(text);
				stream.endText();
			}
			document.save(out);
			return out.toByteArray();
		}
	}

	private static byte[] blankPdf() throws Exception {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			document.addPage(new PDPage());
			document.save(out);
			return out.toByteArray();
		}
	}

	private void stubSuccessfulAnalysis() {
		String responseJson = """
				{
				  "summary": "Standard lease agreement.",
				  "clauseFlags": [
				    {"clauseText": "No refunds under any circumstances.", "riskLevel": "HIGH", "explanation": "Unfair.", "suggestedCorrection": "Refunds within 14 days."}
				  ],
				  "rawResponse": "{\\"id\\":\\"msg_test\\"}"
				}""";
		wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/internal/v1/analyze"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
						.withBody(responseJson)));
	}

	@Test
	void uploadValidPdfIsStoredAndAnalyzed() throws Exception {
		stubSuccessfulAnalysis();
		var file = new MockMultipartFile("file", "lease.pdf", "application/pdf",
				pdfWithText("This is a lease with a no-refunds clause."));

		mockMvc.perform(
				multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, UUID.randomUUID().toString()))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("COMPLETE"))
				.andExpect(jsonPath("$.fileName").value("lease.pdf"));
	}

	@Test
	void uploadRejectsNonPdfContent() throws Exception {
		var file = new MockMultipartFile("file", "not-a-pdf.pdf", "application/pdf", "plain text".getBytes());

		mockMvc.perform(
				multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_PDF"));
	}

	@Test
	void uploadOfNonExtractablePdfMarksContractFailed() throws Exception {
		var file = new MockMultipartFile("file", "scanned.pdf", "application/pdf", blankPdf());

		mockMvc.perform(
				multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, UUID.randomUUID().toString()))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("FAILED"));
	}

	@Test
	void uploadWhenAiAnalysisFailsMarksContractFailed() throws Exception {
		wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/internal/v1/analyze"))
				.willReturn(aResponse().withStatus(500)));
		var file = new MockMultipartFile("file", "lease.pdf", "application/pdf", pdfWithText("Some contract text."));

		mockMvc.perform(
				multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, UUID.randomUUID().toString()))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("FAILED"));
	}

	@Test
	void crossUserAccessToContractIsForbidden() throws Exception {
		stubSuccessfulAnalysis();
		String ownerId = UUID.randomUUID().toString();
		String otherUserId = UUID.randomUUID().toString();
		var file = new MockMultipartFile("file", "lease.pdf", "application/pdf", pdfWithText("Contract text."));

		String body = mockMvc
				.perform(multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, ownerId))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String contractId = objectMapper.readTree(body).get("id").asText();

		mockMvc.perform(get("/api/v1/contracts/" + contractId).header(InternalHeaders.USER_ID, otherUserId))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/contracts/" + contractId).header(InternalHeaders.USER_ID, ownerId))
				.andExpect(status().isOk()).andExpect(jsonPath("$.summary").value("Standard lease agreement."))
				.andExpect(jsonPath("$.clauseFlags[0].riskLevel").value("HIGH"));
	}

	@Test
	void ownerCanDeleteButOthersCannot() throws Exception {
		stubSuccessfulAnalysis();
		String ownerId = UUID.randomUUID().toString();
		String otherUserId = UUID.randomUUID().toString();
		var file = new MockMultipartFile("file", "lease.pdf", "application/pdf", pdfWithText("Contract text."));

		String body = mockMvc
				.perform(multipart("/api/v1/contracts").file(file).header(InternalHeaders.USER_ID, ownerId))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String contractId = objectMapper.readTree(body).get("id").asText();

		mockMvc.perform(delete("/api/v1/contracts/" + contractId).header(InternalHeaders.USER_ID, otherUserId))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/v1/contracts/" + contractId).header(InternalHeaders.USER_ID, ownerId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/contracts/" + contractId).header(InternalHeaders.USER_ID, ownerId))
				.andExpect(status().isNotFound());
	}
}
