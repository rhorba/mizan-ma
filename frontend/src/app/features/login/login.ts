import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly emailNotVerified = signal(false);
  readonly resendState = signal<'idle' | 'sending' | 'sent' | 'cooldown'>('idle');

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.emailNotVerified.set(false);
    const { email, password } = this.form.getRawValue();
    this.authService.login(email, password).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.error?.error?.code === 'EMAIL_NOT_VERIFIED') {
          this.emailNotVerified.set(true);
        } else {
          this.errorMessage.set('Invalid email or password.');
        }
      },
    });
  }

  resend(): void {
    const email = this.form.controls.email.value;
    if (!email) {
      return;
    }
    this.resendState.set('sending');
    this.authService.resendVerification(email).subscribe({
      next: () => this.resendState.set('sent'),
      error: (err) => this.resendState.set(err.status === 429 ? 'cooldown' : 'idle'),
    });
  }
}
