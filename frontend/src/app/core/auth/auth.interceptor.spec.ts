import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('attaches the bearer token when one is present', () => {
    sessionStorage.setItem('mizan_access_token', 'a-token');

    httpClient.get('/api/v1/contracts').subscribe();

    const req = httpMock.expectOne('/api/v1/contracts');
    expect(req.request.headers.get('Authorization')).toBe('Bearer a-token');
    req.flush({});
  });

  it('does not attach a header when there is no token', () => {
    httpClient.get('/api/v1/contracts').subscribe();

    const req = httpMock.expectOne('/api/v1/contracts');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
