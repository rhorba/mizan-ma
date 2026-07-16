import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Register, passwordStrength } from './register';
import { environment } from '../../../environments/environment';

describe('passwordStrength', () => {
  it('scores a short simple password as very weak', () => {
    expect(passwordStrength('abc').score).toBe(0);
  });

  it('scores a long, varied password as strong', () => {
    expect(passwordStrength('Correct-Horse-Battery-9!').score).toBe(4);
  });

  it('scores a plain 10+ char lowercase password as weak', () => {
    expect(passwordStrength('lowercaseonly').score).toBe(1);
  });
});

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let httpMock: HttpTestingController;

  const validForm = {
    email: 'new@example.com',
    password: 'correct-horse-battery',
    confirmPassword: 'correct-horse-battery',
    role: 'INDIVIDUAL' as const,
    tosAccepted: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit when the terms checkbox is unchecked', () => {
    component.form.setValue({ ...validForm, tosAccepted: false });
    component.submit();
    httpMock.expectNone(`${environment.apiBaseUrl}/auth/register`);
  });

  it('does not submit when passwords do not match', () => {
    component.form.setValue({ ...validForm, confirmPassword: 'something-else-entirely' });
    component.submit();
    httpMock.expectNone(`${environment.apiBaseUrl}/auth/register`);
  });

  it('shows the check-your-email state on successful registration', () => {
    component.form.setValue(validForm);

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(req.request.body).toEqual({
      email: validForm.email,
      password: validForm.password,
      role: validForm.role,
    });
    req.flush({ id: 'user-1', email: validForm.email, role: validForm.role });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.registeredEmail()).toBe(validForm.email);
  });

  it('shows a specific message for a duplicate email', () => {
    component.form.setValue(validForm);

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    req.flush({ error: { code: 'EMAIL_ALREADY_EXISTS' } }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(component.errorMessage()).toBe('An account with this email already exists.');
  });

  it('resends the verification email after a successful registration', () => {
    component.form.setValue(validForm);
    component.submit();
    httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`).flush({});

    component.resend();
    const resendReq = httpMock.expectOne(`${environment.apiBaseUrl}/auth/resend-verification`);
    expect(resendReq.request.body).toEqual({ email: validForm.email });
    resendReq.flush(null, { status: 204, statusText: 'No Content' });

    expect(component.resendState()).toBe('sent');
  });
});
