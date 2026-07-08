package ma.mizan.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "clause_flags")
public class ClauseFlag {

	@Id
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	@Column(name = "clause_text", nullable = false)
	private String clauseText;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_level", nullable = false)
	private RiskLevel riskLevel;

	@Column(nullable = false)
	private String explanation;

	@Column(name = "suggested_correction")
	private String suggestedCorrection;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected ClauseFlag() {
	}

	public ClauseFlag(String clauseText, RiskLevel riskLevel, String explanation, String suggestedCorrection) {
		this.id = UUID.randomUUID();
		this.clauseText = clauseText;
		this.riskLevel = riskLevel;
		this.explanation = explanation;
		this.suggestedCorrection = suggestedCorrection;
	}

	public String getClauseText() {
		return clauseText;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public String getExplanation() {
		return explanation;
	}

	public String getSuggestedCorrection() {
		return suggestedCorrection;
	}

	void setAnalysisResult(AnalysisResult analysisResult) {
		this.analysisResult = analysisResult;
	}
}
