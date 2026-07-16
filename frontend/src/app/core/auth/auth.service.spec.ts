import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('starts unauthenticated with no stored token', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.accessToken).toBeNull();
  });

  it('stores tokens and becomes authenticated after login', () => {
    service.login('user@example.com', 'correct-horse-battery').subscribe();
    httpMock
      .expectOne(`${environment.apiBaseUrl}/auth/login`)
      .flush({ accessToken: 'access-1', refreshToken: 'refresh-1', tokenType: 'Bearer', expiresInMs: 3600000 });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.accessToken).toBe('access-1');
    expect(sessionStorage.getItem('mizan_refresh_token')).toBe('refresh-1');
  });

  it('clears tokens and revokes the refresh token on logout', () => {
    service.login('user@example.com', 'correct-horse-battery').subscribe();
    httpMock
      .expectOne(`${environment.apiBaseUrl}/auth/login`)
      .flush({ accessToken: 'access-1', refreshToken: 'refresh-1', tokenType: 'Bearer', expiresInMs: 3600000 });

    service.logout();
    httpMock.expectOne(`${environment.apiBaseUrl}/auth/logout`).flush({});

    expect(service.isAuthenticated()).toBe(false);
    expect(service.accessToken).toBeNull();
  });

  it('logout is a no-op against the network when there is nothing to revoke', () => {
    service.logout();
    httpMock.expectNone(`${environment.apiBaseUrl}/auth/logout`);
  });

  it('posts registration details with the chosen role', () => {
    service.register('new@example.com', 'correct-horse-battery', 'BUSINESS').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(req.request.body).toEqual({ email: 'new@example.com', password: 'correct-horse-battery', role: 'BUSINESS' });
    req.flush({ id: 'user-1', email: 'new@example.com', role: 'BUSINESS' });
  });

  it('posts the verification token', () => {
    service.verifyEmail('raw-token').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/verify-email`);
    expect(req.request.body).toEqual({ token: 'raw-token' });
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('posts the email for resend-verification', () => {
    service.resendVerification('user@example.com').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/resend-verification`);
    expect(req.request.body).toEqual({ email: 'user@example.com' });
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
