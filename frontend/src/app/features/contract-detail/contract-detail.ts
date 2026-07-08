import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ContractsService } from '../../core/services/contracts.service';
import { ContractDetail as ContractDetailModel, RiskLevel } from '../../core/models/contract.model';
import { DisclaimerBanner } from '../../shared/disclaimer-banner/disclaimer-banner';

@Component({
  selector: 'app-contract-detail',
  imports: [RouterLink, MatChipsModule, MatExpansionModule, MatProgressBarModule, DisclaimerBanner],
  templateUrl: './contract-detail.html',
  styleUrl: './contract-detail.scss',
})
export class ContractDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly contractsService = inject(ContractsService);

  readonly contract = signal<ContractDetailModel | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage.set('Analysis failed, retry.');
      this.loading.set(false);
      return;
    }
    this.load(id);
  }

  private load(id: string): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.contractsService.get(id).subscribe({
      next: (contract) => {
        this.contract.set(contract);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Analysis failed, retry.');
        this.loading.set(false);
      },
    });
  }

  riskClass(riskLevel: RiskLevel): string {
    return `risk-${riskLevel.toLowerCase()}`;
  }
}
