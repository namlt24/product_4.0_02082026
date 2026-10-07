import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertPage, AlertResponse, AlertSummary } from '../models/alert.model';

/** 1 nguoi nhan email canh bao cua doi (bang GWM_ALERT_EMAIL - DDL V4). */
export interface AlertEmailRecipient {
  id: string;
  teamCode: string;
  email: string;
  createdAt: string | null;
}

/**
 * Client goi API canh bao (GET/POST /api/alerts/**) - xem AlertController o
 * backend. Trang "Canh bao" goi search/summary polling dinh ky (10s/15s) va
 * acknowledge/resolve khi nguoi dung bam nut.
 */
@Injectable({ providedIn: 'root' })
export class AlertApiService {
  private readonly baseUrl = '/api/alerts';

  constructor(private readonly http: HttpClient) {}

  search(status: string | null, targetName: string | null, page: number, size: number): Observable<AlertPage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (targetName) params = params.set('targetName', targetName);
    return this.http.get<AlertPage>(this.baseUrl, { params });
  }

  summary(): Observable<AlertSummary> {
    return this.http.get<AlertSummary>(`${this.baseUrl}/summary`);
  }

  get(id: string): Observable<AlertResponse> {
    return this.http.get<AlertResponse>(`${this.baseUrl}/${id}`);
  }

  acknowledge(id: string): Observable<AlertResponse> {
    return this.http.post<AlertResponse>(`${this.baseUrl}/${id}/acknowledge`, {});
  }

  resolve(id: string): Observable<AlertResponse> {
    return this.http.post<AlertResponse>(`${this.baseUrl}/${id}/resolve`, {});
  }

  // ---- Nguoi nhan email canh bao (/api/alerts/recipients - AlertEmailRecipientController) ----

  listRecipients(): Observable<AlertEmailRecipient[]> {
    return this.http.get<AlertEmailRecipient[]>(`${this.baseUrl}/recipients`);
  }

  addRecipient(email: string): Observable<AlertEmailRecipient> {
    return this.http.post<AlertEmailRecipient>(`${this.baseUrl}/recipients`, { email });
  }

  deleteRecipient(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipients/${id}`);
  }
}
