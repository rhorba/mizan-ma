package ma.mizan.contracts.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import ma.mizan.common.persistence.UuidEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult extends UuidEntity {

	@OneToOne
	@JoinColumn(name = "contract_id", nullable = false, unique = true)
	private Contract contract;

	@Column(nullable = false)
	private String language;

	@Column(nullable = false)
	private String summary;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "raw_response")
	private String rawResponse;

	@OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ClauseFlag> clauseFlags = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AnalysisResult() {
	}

	public AnalysisResult(Contract contract, String language, String summary, String rawResponse) {
		super(UUID.randomUUID());
		this.contract = contract;
		this.language = language;
		this.summary = summary;
		this.rawResponse = rawResponse;
	}

	public String getLanguage() {
		return language;
	}

	public String getSummary() {
		return summary;
	}

	public String getRawResponse() {
		return rawResponse;
	}

	public List<ClauseFlag> getClauseFlags() {
		return clauseFlags;
	}

	public void addClauseFlag(ClauseFlag flag) {
		clauseFlags.add(flag);
		flag.setAnalysisResult(this);
	}
}
