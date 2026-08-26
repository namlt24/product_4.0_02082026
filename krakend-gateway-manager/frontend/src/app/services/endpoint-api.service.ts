import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DependencyGraph,
  DeployResult,
  EndpointConfig,
  GatewayInfo,
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
