import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { ContractDetail } from './contract-detail';
import { environment } from '../../../environments/environment';

describe('ContractDetail', () => {
  let component: ContractDetail;
  let fixture: ComponentFixture<ContractDetail>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContractDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: 'contract-1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContractDetail);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and render the summary and clause flags', () => {
    expect(component).toBeTruthy();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`);
    req.flush({
      id: 'contract-1',
      fileName: 'lease.pdf',
      status: 'COMPLETE',
      createdAt: new Date().toISOString(),
      summary: 'Standard lease.',
      clauseFlags: [
        {
          clauseText: 'No refunds under any circumstances.',
          riskLevel: 'HIGH',
          explanation: 'Unfair to the tenant.',
          suggestedCorrection: 'Refunds within 14 days.',
        },
        {
          clauseText: 'Notice period is 30 days.',
          riskLevel: 'LOW',
          explanation: 'Standard notice period.',
          suggestedCorrection: null,
        },
      ],
    });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.contract()?.summary).toBe('Standard lease.');
    expect(fixture.nativeElement.querySelectorAll('mat-expansion-panel').length).toBe(2);
  });

  it('maps risk levels to the matching CSS class', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`).flush({
      id: 'contract-1',
      fileName: 'lease.pdf',
      status: 'COMPLETE',
      createdAt: new Date().toISOString(),
      summary: 'Standard lease.',
      clauseFlags: [],
    });
    expect(component.riskClass('HIGH')).toBe('risk-high');
  });

  it('surfaces a FAILED contract without treating it as a load error', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`).flush({
      id: 'contract-1',
      fileName: 'scanned.pdf',
      status: 'FAILED',
      createdAt: new Date().toISOString(),
      summary: null,
      clauseFlags: [],
    });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBeNull();
    expect(component.contract()?.status).toBe('FAILED');
  });

  it('shows an in-progress state for a non-terminal status', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/contract-1`).flush({
      id: 'contract-1',
      fileName: 'lease.pdf',
      status: 'ANALYZING',
      createdAt: new Date().toISOString(),
      summary: null,
      clauseFlags: [],
    });
    fixture.detectChanges();

    expect(component.contract()?.status).toBe('ANALYZING');
    expect(fixture.nativeElement.textContent).toContain('Analysis in progress');
  });

  it('shows an error message when the load request fails', () => {
    httpMock
      .expectOne(`${environment.apiBaseUrl}/contracts/contract-1`)
      .flush('error', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Analysis failed, retry.');
  });
});

describe('ContractDetail without a route id', () => {
  it('shows an error and never issues a request', async () => {
    await TestBed.configureTestingModule({
      imports: [ContractDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({}) } } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ContractDetail);
    const component = fixture.componentInstance;
    const httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Analysis failed, retry.');
    httpMock.verify();
  });
});
