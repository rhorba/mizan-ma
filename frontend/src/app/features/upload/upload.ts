import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ContractsService } from '../../core/services/contracts.service';

@Component({
  selector: 'app-upload',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatProgressBarModule,
    MatSelectModule,
  ],
  templateUrl: './upload.html',
  styleUrl: './upload.scss',
})
export class Upload {
  private readonly contractsService = inject(ContractsService);
  private readonly router = inject(Router);

  readonly selectedFile = signal<File | null>(null);
  readonly language = signal('fr');
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
    this.errorMessage.set(null);
  }

  submit(): void {
    const file = this.selectedFile();
    if (!file) {
      return;
    }
    this.uploading.set(true);
    this.errorMessage.set(null);
    this.contractsService.upload(file, this.language()).subscribe({
      next: (contract) => {
        this.uploading.set(false);
        this.router.navigate(['/contracts', contract.id]);
      },
      error: () => {
        this.uploading.set(false);
        this.errorMessage.set("Upload failed — couldn't read this PDF. Try a different file.");
      },
    });
  }
}
