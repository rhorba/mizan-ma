import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { Login } from './login';
import { environment } from '../../../environments/environment';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit an invalid form', () => {
    component.form.setValue({ email: 'not-an-email', password: '' });
    component.submit();
    httpMock.expectNone(`${environment.apiBaseUrl}/auth/login`);
  });

  it('navigates to the dashboard on successful login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    component.form.setValue({ email: 'user@example.com', password: 'correct-horse-battery' });

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    req.flush({ accessToken: 'a', refreshToken: 'b', tokenType: 'Bearer', expiresInMs: 3600000 });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('shows an error message on failed login', () => {
    component.form.setValue({ email: 'user@example.com', password: 'wrong-password' });

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    req.flush({ error: { code: 'INVALID_CREDENTIALS' } }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Invalid email or password.');
  });
});
