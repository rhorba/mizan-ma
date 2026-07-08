import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../core/auth/auth.service';
import { ContractsService } from '../../core/services/contracts.service';
import { ContractStatus, ContractSummary } from '../../core/models/contract.model';
import { DisclaimerBanner } from '../../shared/disclaimer-banner/disclaimer-banner';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatToolbarModule,
    DisclaimerBanner,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly contractsService = inject(ContractsService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly displayedColumns = ['fileName', 'status', 'createdAt'];
  readonly contracts = signal<ContractSummary[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.contractsService.list().subscribe({
      next: (contracts) => {
        this.contracts.set(contracts);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set("Couldn't load contracts, retry.");
        this.loading.set(false);
      },
    });
  }

  statusClass(status: ContractStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  openContract(id: string): void {
    this.router.navigate(['/contracts', id]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
