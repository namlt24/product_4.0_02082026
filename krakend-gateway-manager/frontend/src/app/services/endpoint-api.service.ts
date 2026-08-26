import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ConfigExportBundle,
  ConfigImportResult,
  DependencyGraph,
  DeployResult,
  EndpointConfig,
  EndpointTryRequest,
  EndpointVersionSummary,
  GatewayInfo,
  UpstreamHealth,
  UpstreamService,
} from '../models/endpoint.model';

/**
 * Client goi Control Plane API (Spring Boot backend).
 * Base path la "/api" - trong dev duoc Angular CLI proxy sang localhost:8080
 * (xem proxy.conf.json), trong production Nginx cua frontend proxy sang
 * container "backend" (xem nginx.conf).
 */
@Injectable({ providedIn: 'root' })
export class EndpointApiService {
  private readonly endpointsUrl = '/api/endpoints';
  private readonly upstreamsUrl = '/api/upstreams';
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

  /**
   * Validate (canh bao vong lap phu thuoc) + nap lai cache dinh tuyen trong-process.
   * Luu y: khac voi truoc day, MOI thay doi qua create/update/delete DA co hieu
   * luc ngay tuc thi - goi endpoint nay chi de validate roi truoc mot loat thay
   * doi, khong con bat buoc phai bam truoc khi API moi hoat dong.
   */
  deploy(): Observable<DeployResult> {
    return this.http.post<DeployResult>(`${this.configUrl}/deploy`, {});
  }

  /** So do phu thuoc giua cac endpoint (endpoint nao goi nguoc vao endpoint nao qua chinh gateway). */
  getDependencyGraph(): Observable<DependencyGraph> {
    return this.http.get<DependencyGraph>(`${this.endpointsUrl}/dependency-graph`);
  }

  /** Thong tin gateway (port, host alias) - dung de tu dien Endpoint Picker. */
  getGatewayInfo(): Observable<GatewayInfo> {
    return this.http.get<GatewayInfo>(`${this.configUrl}/gateway-info`);
  }

  // ---- Lich su phien ban (xem EndpointVersionService o backend) ----

  listVersions(endpointId: string): Observable<EndpointVersionSummary[]> {
    return this.http.get<EndpointVersionSummary[]>(`${this.endpointsUrl}/${endpointId}/versions`);
  }

  getVersion(endpointId: string, versionId: string): Observable<EndpointConfig> {
    return this.http.get<EndpointConfig>(`${this.endpointsUrl}/${endpointId}/versions/${versionId}`);
  }

  rollbackVersion(endpointId: string, versionId: string): Observable<EndpointConfig> {
    return this.http.post<EndpointConfig>(`${this.endpointsUrl}/${endpointId}/versions/${versionId}/rollback`, {});
  }

  // ---- P1: Thu ngay / OpenAPI / Suc khoe Upstream / Export-Import ----

  /** Goi that qua Control Plane, dung dung composite engine - xem EndpointTryService o backend. */
  tryEndpoint(endpointId: string, req: EndpointTryRequest): Observable<unknown> {
    return this.http.post(`${this.endpointsUrl}/${endpointId}/try`, req);
  }

  getOpenApiSpec(endpointId: string): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`${this.endpointsUrl}/${endpointId}/openapi`);
  }

  getUpstreamHealth(): Observable<UpstreamHealth[]> {
    return this.http.get<UpstreamHealth[]>(`${this.upstreamsUrl}/health`);
  }

  exportConfig(): Observable<ConfigExportBundle> {
    return this.http.get<ConfigExportBundle>(`${this.configUrl}/export`);
  }

  importConfig(bundle: ConfigExportBundle): Observable<ConfigImportResult> {
    return this.http.post<ConfigImportResult>(`${this.configUrl}/import`, bundle);
  }

  // ---- Upstream Services (backend that, dang ky 1 lan, dung chung cho nhieu BackendStep) ----

  listUpstreams(): Observable<UpstreamService[]> {
    return this.http.get<UpstreamService[]>(this.upstreamsUrl);
  }

  getUpstream(id: string): Observable<UpstreamService> {
    return this.http.get<UpstreamService>(`${this.upstreamsUrl}/${id}`);
  }

  createUpstream(payload: UpstreamService): Observable<UpstreamService> {
    return this.http.post<UpstreamService>(this.upstreamsUrl, payload);
  }

  updateUpstream(id: string, payload: UpstreamService): Observable<UpstreamService> {
    return this.http.put<UpstreamService>(`${this.upstreamsUrl}/${id}`, payload);
  }

  deleteUpstream(id: string): Observable<void> {
    return this.http.delete<void>(`${this.upstreamsUrl}/${id}`);
  }
}
