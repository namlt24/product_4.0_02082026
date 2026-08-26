import { Injectable } from '@angular/core';

const STORAGE_KEY = 'gwm-admin-key';

/**
 * Luu API key (X-Gateway-Admin-Key) cua phien dang nhap hien tai trong
 * sessionStorage - chi ton tai trong tab hien tai, mat khi dong tab (khac
 * localStorage, khong luu qua lau tren may dung chung). Dung chung boi
 * LoginComponent, apiKeyInterceptor, authGuard - 1 nguon duy nhat cho storage key.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  getKey(): string | null {
    try {
      return sessionStorage.getItem(STORAGE_KEY);
    } catch {
      return null;
    }
  }

  setKey(key: string): void {
    try {
      sessionStorage.setItem(STORAGE_KEY, key);
    } catch {
      // sessionStorage khong kha dung (che do rieng tu nghiem ngat...) - bo qua,
      // request se di khong header va bi 401, nguoi dung quay lai man hinh login.
    }
  }

  clearKey(): void {
    try {
      sessionStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
  }

  isAuthenticated(): boolean {
    return !!this.getKey();
  }
}
