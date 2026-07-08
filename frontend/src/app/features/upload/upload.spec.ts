import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { Upload } from './upload';
import { environment } from '../../../environments/environment';

describe('Upload', () => {
  let component: Upload;
  let fixture: ComponentFixture<Upload>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Upload],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Upload);
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

  it('does nothing when submitting without a selected file', () => {
    component.submit();
    httpMock.expectNone(`${environment.apiBaseUrl}/contracts?language=fr`);
  });

  it('tracks the selected file from the input change event', () => {
    const file = new File(['%PDF-1.4'], 'lease.pdf', { type: 'application/pdf' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });

    component.onFileSelected({ target: input } as unknown as Event);
    fixture.detectChanges();

    expect(component.selectedFile()?.name).toBe('lease.pdf');
  });

  it('navigates to the contract on successful upload', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    const file = new File(['%PDF-1.4'], 'lease.pdf', { type: 'application/pdf' });
    component.selectedFile.set(file);

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts?language=fr`);
    req.flush({ id: 'contract-1', fileName: 'lease.pdf', status: 'COMPLETE', createdAt: new Date().toISOString() });
    fixture.detectChanges();

    expect(component.uploading()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/contracts', 'contract-1']);
  });

  it('shows an error message on failed upload', () => {
    const file = new File(['not a pdf'], 'fake.pdf', { type: 'application/pdf' });
    component.selectedFile.set(file);

    component.submit();
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contracts?language=fr`);
    req.flush({ error: { code: 'INVALID_PDF' } }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    expect(component.uploading()).toBe(false);
    expect(component.errorMessage()).toContain("couldn't read this PDF");
  });
});
