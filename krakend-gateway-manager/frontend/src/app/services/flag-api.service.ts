import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GwmFlag, GwmFlagHistory } from '../models/flag.model';

/**
 * Client goi API CỜ NGHIỆP VỤ (GET/POST /api/flags/**) - xem FlagController o
 * backend. Trang "Cờ nghiệp vụ" dùng CRUD + toggle; lịch sử đổi giá trị mở
 * rộng từ từng row trong bảng.
 */
@Injectable({ providedIn: 'root' })
export class FlagApiService {
  private readonly baseUrl = '/api/flags';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<GwmFlag[]> {
    return this.http.get<GwmFlag[]>(this.baseUrl);
  }

  /** Tạo cờ mới - flagCode duy nhất theo team (backend trả GW-FLAG-EXIST nếu trùng). */
  create(flagCode: string, displayName: string, description: string, enabled: boolean): Observable<GwmFlag> {
    return this.http.post<GwmFlag>(this.baseUrl, { flagCode, displayName, description, enabled });
  }

  update(id: string, displayName: string, description: string): Observable<GwmFlag> {
    return this.http.put<GwmFlag>(`${this.baseUrl}/${id}`, { displayName, description });
  }

  /** Toggle BẬT/TẮT cờ - thao tác chính khi chạy thật (DP sync giá trị trong 15s). */
  toggle(id: string): Observable<GwmFlag> {
    return this.http.post<GwmFlag>(`${this.baseUrl}/${id}/toggle`, {});
  }

  history(id: string): Observable<GwmFlagHistory[]> {
    return this.http.get<GwmFlagHistory[]>(`${this.baseUrl}/${id}/history`);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
