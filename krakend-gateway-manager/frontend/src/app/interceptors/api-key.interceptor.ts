import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Gan header X-Gateway-Admin-Key cho moi request toi Control Plane (/api/**) -
 * Data Plane (goi API composite that qua DynamicDispatcherController) khong
 * nam duoi /api nen khong bi dung toi, dung y pham vi ApiKeyAuthFilter ben
 * backend. Nhan 401 (thieu/sai/het han key) -> xoa key da luu + dieu huong
 * ve /login kem returnUrl (tru khi da dang o /login - tranh redirect thua
 * khi chinh request thu-dang-nhap bi 401 vi go sai key).
 */
export const apiKeyInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!req.url.startsWith('/api')) {
    return next(req);
  }

  const key = auth.getKey();
  const authReq = key ? req.clone({ setHeaders: { 'X-Gateway-Admin-Key': key } }) : req;

  return next(authReq).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && !router.url.startsWith('/login')) {
        auth.clearKey();
        router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      }
      return throwError(() => err);
    }),
  );
};
