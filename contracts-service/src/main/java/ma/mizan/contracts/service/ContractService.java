package ma.mizan.contracts.service;

import java.util.List;
import java.util.UUID;
import ma.mizan.contracts.client.AiAnalysisClient;
import ma.mizan.contracts.client.dto.AnalysisOutcome;
import ma.mizan.contracts.client.dto.ClauseFlagResult;
import ma.mizan.contracts.controller.dto.ContractDetailResponse;
import ma.mizan.contracts.controller.dto.ContractSummaryResponse;
import ma.mizan.contracts.domain.AnalysisResult;
import ma.mizan.contracts.domain.ClauseFlag;
import ma.mizan.contracts.domain.Contract;
import ma.mizan.contracts.domain.ContractStatus;
import ma.mizan.contracts.domain.RiskLevel;
import ma.mizan.contracts.exception.AiAnalysisException;
import ma.mizan.contracts.exception.ContractAccessDeniedException;
import ma.mizan.contracts.exception.ContractNotFoundException;
import ma.mizan.contracts.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContractService {

	private final ContractRepository contractRepository;
	private final R2StorageService r2StorageService;
	private final PdfValidator pdfValidator;
	private final PdfTextExtractor pdfTextExtractor;
	private final AiAnalysisClient aiAnalysisClient;

	public ContractService(ContractRepository contractRepository, R2StorageService r2StorageService,
			PdfValidator pdfValidator, PdfTextExtractor pdfTextExtractor, AiAnalysisClient aiAnalysisClient) {
		this.contractRepository = contractRepository;
		this.r2StorageService = r2StorageService;
		this.pdfValidator = pdfValidator;
		this.pdfTextExtractor = pdfTextExtractor;
		this.aiAnalysisClient = aiAnalysisClient;
	}

	@Transactional
	public ContractSummaryResponse upload(UUID userId, MultipartFile file, String language) {
		pdfValidator.validate(file);
		byte[] pdfBytes = readBytes(file);

		String objectKey = userId + "/" + UUID.randomUUID() + ".pdf";
		r2StorageService.upload(objectKey, pdfBytes, "application/pdf");

		Contract contract = new Contract(userId, file.getOriginalFilename(), objectKey);
		contractRepository.save(contract);

		runAnalysis(contract, pdfBytes, language);

		return ContractSummaryResponse.from(contract);
	}

	@Transactional(readOnly = true)
	public List<ContractSummaryResponse> listOwn(UUID userId) {
		return contractRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ContractSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ContractDetailResponse getOwn(UUID userId, UUID contractId) {
		Contract contract = findOwned(userId, contractId, contractRepository.findByIdWithAnalysis(contractId));
		return ContractDetailResponse.from(contract);
	}

	@Transactional
	public void deleteOwn(UUID userId, UUID contractId) {
		Contract contract = findOwned(userId, contractId, contractRepository.findById(contractId));
		r2StorageService.delete(contract.getR2ObjectKey());
		contractRepository.delete(contract);
	}

	private Contract findOwned(UUID userId, UUID contractId, java.util.Optional<Contract> maybeContract) {
		Contract contract = maybeContract.orElseThrow(() -> new ContractNotFoundException(contractId));
		if (!contract.isOwnedBy(userId)) {
			throw new ContractAccessDeniedException();
		}
		return contract;
	}

	private void runAnalysis(Contract contract, byte[] pdfBytes, String language) {
		contract.setStatus(ContractStatus.ANALYZING);

		PdfTextExtractor.Extraction extraction;
		try {
			extraction = pdfTextExtractor.extract(pdfBytes);
		} catch (Exception e) {
			contract.setStatus(ContractStatus.FAILED);
			return;
		}
		contract.setPageCount(extraction.pageCount());

		if (extraction.text() == null || extraction.text().isBlank()) {
			// Non-extractable (scanned/image-only) PDF — no text layer to analyze.
			contract.setStatus(ContractStatus.FAILED);
			return;
		}

		try {
			AnalysisOutcome outcome = aiAnalysisClient.analyze(extraction.text(), language);
			var analysisResult = new AnalysisResult(contract, language, outcome.summary(), outcome.rawResponse());
			for (ClauseFlagResult flag : outcome.clauseFlags()) {
				analysisResult.addClauseFlag(
						new ClauseFlag(flag.clauseText(), RiskLevel.valueOf(flag.riskLevel().toUpperCase()),
								flag.explanation(), flag.suggestedCorrection()));
			}
			contract.attachAnalysisResult(analysisResult);
			contract.setStatus(ContractStatus.COMPLETE);
			contractRepository.save(contract);
		} catch (AiAnalysisException e) {
			contract.setStatus(ContractStatus.FAILED);
		}
	}

	private byte[] readBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (Exception e) {
			throw new ma.mizan.contracts.exception.InvalidPdfException("Could not read file content");
		}
	}
}
