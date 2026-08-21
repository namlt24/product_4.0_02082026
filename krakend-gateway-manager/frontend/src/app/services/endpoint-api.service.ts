import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DependencyGraph,
  DeployResult,
  EndpointConfig,
  GatewayInfo,
  PreviewResponse,
} from '../models/endpoint.model';

/**
 * Client goi Control Plane API (Spring Boot backend).
 * Base path la "/api" - trong dev duoc Angular CLI proxy sang localhost:9000
 * (xem proxy.conf.json), trong production Nginx cua frontend proxy sang
 * container "backend" (xem nginx.conf).
 */
@Injectable({ providedIn: 'root' })
export class EndpointApiService {
  private readonly endpointsUrl = '/api/endpoints';
  private readonly configUrl = '/api/config';

  constructor(private readonly http: HttpClient) {}

  list(search?: string): Observable<EndpointConfig[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('q', search);
    }
    return this.http.get<EndpointConfig[]>(this.endpointsUrl, { params });
  }

  get(id: string): Observable<EndpointConfig> {
    return this.http.get<EndpointConfig>(`${this.endpointsUrl}/${id}`);
  }

  create(payload: EndpointConfig): Observable<EndpointConfig> {
    return this.http.post<EndpointConfig>(this.endpointsUrl, payload);
  }

  update(id: string, payload: EndpointConfig): Observable<EndpointConfig> {
    return this.http.put<EndpointConfig>(`${this.endpointsUrl}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.endpointsUrl}/${id}`);
  }

  /** Preview JSON cho 1 endpoint da luu. */
  previewSaved(id: string): Observable<PreviewResponse> {
    return this.http.get<PreviewResponse>(`${this.endpointsUrl}/${id}/preview`);
  }

  /** Preview JSON "truoc khi luu" - gui thang du lieu tu form, khong dong bo DB. */
  previewDraft(payload: EndpointConfig): Observable<PreviewResponse> {
    return this.http.post<PreviewResponse>(`${this.endpointsUrl}/preview`, payload);
  }

  /** Preview toan bo krakend.json duoc gop tu tat ca endpoint dang luu. */
  previewAll(): Observable<PreviewResponse> {
    return this.http.get<PreviewResponse>(`${this.configUrl}/preview`);
  }

  /** Ghi krakend.json + reload container KrakenD. */
  deploy(): Observable<DeployResult> {
    return this.http.post<DeployResult>(`${this.configUrl}/deploy`, {});
  }

  /** So do phu thuoc giua cac endpoint (endpoint nao goi nguoc vao endpoint nao qua chinh KrakenD). */
  getDependencyGraph(): Observable<DependencyGraph> {
    return this.http.get<DependencyGraph>(`${this.endpointsUrl}/dependency-graph`);
  }

  /** Thong tin gateway (port, host alias) - dung de tu dien Endpoint Picker. */
  getGatewayInfo(): Observable<GatewayInfo> {
    return this.http.get<GatewayInfo>(`${this.configUrl}/gateway-info`);
  }
}
