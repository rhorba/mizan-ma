package ma.mizan.contracts.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import ma.mizan.common.persistence.UuidEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "contracts")
public class Contract extends UuidEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "r2_object_key", nullable = false)
	private String r2ObjectKey;

	@Column(name = "page_count")
	private Integer pageCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContractStatus status = ContractStatus.PENDING;

	@OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
	private AnalysisResult analysisResult;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Contract() {
	}

	public Contract(UUID userId, String fileName, String r2ObjectKey) {
		super(UUID.randomUUID());
		this.userId = userId;
		this.fileName = fileName;
		this.r2ObjectKey = r2ObjectKey;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getR2ObjectKey() {
		return r2ObjectKey;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public ContractStatus getStatus() {
		return status;
	}

	public void setStatus(ContractStatus status) {
		this.status = status;
	}

	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	public void attachAnalysisResult(AnalysisResult analysisResult) {
		this.analysisResult = analysisResult;
	}

	public boolean isOwnedBy(UUID candidateUserId) {
		return userId.equals(candidateUserId);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
