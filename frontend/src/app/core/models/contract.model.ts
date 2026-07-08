export type ContractStatus = 'PENDING' | 'ANALYZING' | 'COMPLETE' | 'FAILED';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface ContractSummary {
  id: string;
  fileName: string;
  status: ContractStatus;
  createdAt: string;
}

export interface ClauseFlag {
  clauseText: string;
  riskLevel: RiskLevel;
  explanation: string;
  suggestedCorrection: string | null;
}

export interface ContractDetail {
  id: string;
  fileName: string;
  status: ContractStatus;
  createdAt: string;
  summary: string | null;
  clauseFlags: ClauseFlag[];
}

export interface ContractStats {
  byStatus: Record<string, number>;
  byRiskLevel: Record<string, number>;
}
