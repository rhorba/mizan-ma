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
});
