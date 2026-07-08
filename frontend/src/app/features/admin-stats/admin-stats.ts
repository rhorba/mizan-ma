import { Component, OnInit, inject, signal } from '@angular/core';
import { KeyValuePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ContractsService } from '../../core/services/contracts.service';
import { ContractStats } from '../../core/models/contract.model';

@Component({
  selector: 'app-admin-stats',
  imports: [KeyValuePipe, MatCardModule, MatProgressBarModule, MatToolbarModule],
  templateUrl: './admin-stats.html',
  styleUrl: './admin-stats.scss',
})
export class AdminStats implements OnInit {
  private readonly contractsService = inject(ContractsService);

  readonly stats = signal<ContractStats | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.contractsService.stats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set("Couldn't load stats.");
        this.loading.set(false);
      },
    });
  }

  hasData(record: Record<string, number>): boolean {
    return Object.keys(record).length > 0;
  }
}
