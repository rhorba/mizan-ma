import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
}

export type RegistrableRole = 'INDIVIDUAL' | 'BUSINESS';

const ACCESS_TOKEN_KEY = 'mizan_access_token';
const REFRESH_TOKEN_KEY = 'mizan_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly accessTokenSignal = signal<string | null>(sessionStorage.getItem(ACCESS_TOKEN_KEY));
  readonly isAuthenticated = computed(() => this.accessTokenSignal() !== null);

  get accessToken(): string | null {
    return this.accessTokenSignal();
  }

  login(email: string, password: string): Observable<void> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password })
      .pipe(
        tap((response) => this.storeTokens(response)),
        map(() => undefined),
      );
  }

  register(email: string, password: string, role: RegistrableRole): Observable<void> {
    return this.http
      .post(`${environment.apiBaseUrl}/auth/register`, { email, password, role })
      .pipe(map(() => undefined));
  }

  verifyEmail(token: string): Observable<void> {
    return this.http.post(`${environment.apiBaseUrl}/auth/verify-email`, { token }).pipe(map(() => undefined));
  }

  resendVerification(email: string): Observable<void> {
    return this.http
      .post(`${environment.apiBaseUrl}/auth/resend-verification`, { email })
      .pipe(map(() => undefined));
  }

  logout(): void {
    const refreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    this.accessTokenSignal.set(null);
    if (refreshToken) {
      // Best-effort server-side revocation — the client has already discarded its tokens either way.
      this.http.post(`${environment.apiBaseUrl}/auth/logout`, { refreshToken }).subscribe();
    }
  }

  private storeTokens(response: AuthResponse): void {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    this.accessTokenSignal.set(response.accessToken);
  }
}
