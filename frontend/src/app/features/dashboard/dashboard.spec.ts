import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { Dashboard } from './dashboard';
import { environment } from '../../../environments/environment';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load contracts', () => {
    expect(component).toBeTruthy();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts`);
    req.flush([]);
    fixture.detectChanges();
    expect(component.loading()).toBe(false);
    expect(component.contracts()).toEqual([]);
  });

  it('renders the table when contracts exist', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([
      { id: 'contract-1', fileName: 'lease.pdf', status: 'COMPLETE', createdAt: new Date().toISOString() },
    ]);
    fixture.detectChanges();

    expect(component.contracts().length).toBe(1);
    expect(fixture.nativeElement.querySelector('table')).toBeTruthy();
  });

  it('shows an error message and allows retry', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush('error', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(component.errorMessage()).toBe("Couldn't load contracts, retry.");

    component.loadContracts();
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([]);
    fixture.detectChanges();
    expect(component.errorMessage()).toBeNull();
  });

  it('navigates to the contract detail page when a row is opened', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([]);
    const navigateSpy = vi.spyOn(router, 'navigate');

    component.openContract('contract-1');

    expect(navigateSpy).toHaveBeenCalledWith(['/contracts', 'contract-1']);
  });

  it('maps status to the matching CSS class', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([]);
    expect(component.statusClass('COMPLETE')).toBe('status-complete');
  });

  it('clears the token and navigates to login on logout', () => {
    httpMock.expectOne(`${environment.apiBaseUrl}/contracts`).flush([]);
    const navigateSpy = vi.spyOn(router, 'navigate');

    component.logout();

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
