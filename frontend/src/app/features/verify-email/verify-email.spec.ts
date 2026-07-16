import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { VerifyEmail } from './verify-email';
import { environment } from '../../../environments/environment';

function setUp(token: string | null): { fixture: ComponentFixture<VerifyEmail>; httpMock: HttpTestingController } {
  TestBed.configureTestingModule({
    imports: [VerifyEmail],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      provideRouter([]),
      {
        provide: ActivatedRoute,
        useValue: {
          snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) },
        },
      },
    ],
  });

  const fixture = TestBed.createComponent(VerifyEmail);
  const httpMock = TestBed.inject(HttpTestingController);
  return { fixture, httpMock };
}

describe('VerifyEmail', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('shows an error immediately when there is no token in the URL', () => {
    const { fixture, httpMock } = setUp(null);
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('error');
    httpMock.verify();
  });

  it('shows success once the token is verified', () => {
    const { fixture, httpMock } = setUp('a-real-token');
    fixture.detectChanges();

    httpMock.expectOne(`${environment.apiBaseUrl}/auth/verify-email`).flush(null, { status: 204, statusText: 'No Content' });

    expect(fixture.componentInstance.status()).toBe('success');
    httpMock.verify();
  });

  it('shows an error when the token is rejected', () => {
    const { fixture, httpMock } = setUp('expired-token');
    fixture.detectChanges();

    httpMock
      .expectOne(`${environment.apiBaseUrl}/auth/verify-email`)
      .flush({ error: { code: 'INVALID_VERIFICATION_TOKEN' } }, { status: 400, statusText: 'Bad Request' });

    expect(fixture.componentInstance.status()).toBe('error');
    httpMock.verify();
  });
});
