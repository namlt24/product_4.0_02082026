import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HopAuditEvent, LogSearchFilter, LogSearchResult } from '../models/log-search.model';

/**
 * Client goi API tra cuu log audit (GET /api/logs/**) - xem LogSearchController
 * o backend. Tach rieng voi EndpointApiService vi day la domain doc lap
 * (chi doc, khong lien quan CRUD Endpoint/Upstream) - dung chung 1 service se
 * lam file do phinh to khong can thiet.
 */
@Injectable({ providedIn: 'root' })
export class LogSearchApiService {
  private readonly baseUrl = '/api/logs/requests';

  constructor(private readonly http: HttpClient) {}

  search(filter: LogSearchFilter, page: number, size: number): Observable<LogSearchResult> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filter.from) params = params.set('from', filter.from);
    if (filter.to) params = params.set('to', filter.to);
    if (filter.status) params = params.set('status', filter.status);
    if (filter.endpointPath) params = params.set('endpointPath', filter.endpointPath);
    if (filter.bodyContains) params = params.set('bodyContains', filter.bodyContains);
    return this.http.get<LogSearchResult>(this.baseUrl, { params });
  }

  getHops(requestId: string): Observable<HopAuditEvent[]> {
    return this.http.get<HopAuditEvent[]>(`${this.baseUrl}/${requestId}/hops`);
  }
}
