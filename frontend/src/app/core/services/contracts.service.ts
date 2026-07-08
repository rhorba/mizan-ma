import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ContractDetail, ContractSummary } from '../models/contract.model';

@Injectable({ providedIn: 'root' })
export class ContractsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/contracts`;

  list(): Observable<ContractSummary[]> {
    return this.http.get<ContractSummary[]>(this.baseUrl);
  }

  get(id: string): Observable<ContractDetail> {
    return this.http.get<ContractDetail>(`${this.baseUrl}/${id}`);
  }

  upload(file: File, language: string): Observable<ContractSummary> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ContractSummary>(`${this.baseUrl}?language=${encodeURIComponent(language)}`, formData);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
