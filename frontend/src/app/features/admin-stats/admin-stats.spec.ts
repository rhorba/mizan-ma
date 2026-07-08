import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AdminStats } from './admin-stats';
import { environment } from '../../../environments/environment';

describe('AdminStats', () => {
  let component: AdminStats;
  let fixture: ComponentFixture<AdminStats>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminStats],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminStats);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load stats', () => {
    expect(component).toBeTruthy();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts/stats`);
    req.flush({ byStatus: { COMPLETE: 2, FAILED: 1 }, byRiskLevel: { HIGH: 1 } });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.stats()?.byStatus['COMPLETE']).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('table').length).toBe(2);
  });

  it('shows the empty state when there is no data', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/stats`).flush({ byStatus: {}, byRiskLevel: {} });
    fixture.detectChanges();

    expect(component.hasData({})).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('No data yet');
  });

  it('shows an error message and allows retry', () => {
    httpMock
      .expectOne(`${environment.apiBaseUrl}/contracts/stats`)
      .flush('error', { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();
    expect(component.errorMessage()).toBe("Couldn't load stats.");

    component.loadStats();
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts/stats`).flush({ byStatus: {}, byRiskLevel: {} });
    fixture.detectChanges();
    expect(component.errorMessage()).toBeNull();
  });
});
