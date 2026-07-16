import { Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { AuthService, RegistrableRole } from '../../core/auth/auth.service';

function passwordsMatchValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  };
}

export interface PasswordStrength {
  score: 0 | 1 | 2 | 3 | 4;
  label: 'Very weak' | 'Weak' | 'Fair' | 'Good' | 'Strong';
}

export function passwordStrength(password: string): PasswordStrength {
  let score = 0;
  if (password.length >= 10) score++;
  if (password.length >= 16) score++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/\d/.test(password) && /[^a-zA-Z0-9]/.test(password)) score++;

  const labels: PasswordStrength['label'][] = ['Very weak', 'Weak', 'Fair', 'Good', 'Strong'];
  return { score: score as PasswordStrength['score'], label: labels[score] };
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly registeredEmail = signal<string | null>(null);
  readonly resendState = signal<'idle' | 'sending' | 'sent' | 'cooldown'>('idle');

  readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(10)]],
      confirmPassword: ['', Validators.required],
      role: ['INDIVIDUAL' as RegistrableRole, Validators.required],
      tosAccepted: [false, Validators.requiredTrue],
    },
    { validators: passwordsMatchValidator() },
  );

  readonly strength = computed(() => passwordStrength(this.form.controls.password.value ?? ''));
  readonly strengthPercent = computed(() => (this.strength().score / 4) * 100);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    const { email, password, role } = this.form.getRawValue();
    this.authService.register(email, password, role).subscribe({
      next: () => {
        this.loading.set(false);
        this.registeredEmail.set(email);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.error?.error?.code === 'EMAIL_ALREADY_EXISTS') {
          this.errorMessage.set('An account with this email already exists.');
        } else {
          this.errorMessage.set('Registration failed. Please check your details and try again.');
        }
      },
    });
  }

  resend(): void {
    const email = this.registeredEmail();
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
