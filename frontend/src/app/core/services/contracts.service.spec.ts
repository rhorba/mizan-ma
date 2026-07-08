import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ContractsService } from './contracts.service';
import { environment } from '../../../environments/environment';

describe('ContractsService', () => {
  let service: ContractsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ContractsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists own contracts', () => {
    service.list().subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([]);
  });

  it('gets a single contract', () => {
    service.get('contract-1').subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`).flush({});
  });

  it('uploads a file with the given language', () => {
    const file = new File(['%PDF-1.4'], 'lease.pdf', { type: 'application/pdf' });
    service.upload(file, 'fr').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts?language=fr`);
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush({});
  });

  it('deletes a contract', () => {
    service.delete('contract-1').subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`).flush(null);
  });

  it('gets aggregate stats', () => {
    service.stats().subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/stats`).flush({ byStatus: {}, byRiskLevel: {} });
  });
});
