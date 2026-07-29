import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from '../api';
import { AuthService } from './auth.service';

/**
 * Two jobs, on every request the app makes:
 *
 * 1. Turn on withCredentials for our own API. The session cookie is set by
 *    localhost:8080 while the app runs on localhost:4200, and the browser only
 *    attaches cookies cross-origin when the request asks for it explicitly.
 * 2. Notice a 401 and send the user to the login page, which is what happens
 *    when the token quietly expires while the app is open.
 *
 * Note there is no Authorization header to set: the cookie IS the credential,
 * and it is HttpOnly precisely so that this code cannot touch it.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const isOurApi = req.url.startsWith(API_BASE_URL);

  // HttpRequest is immutable, so we clone instead of mutating.
  const request = isOurApi ? req.clone({ withCredentials: true }) : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // The auth endpoints answer 401 as part of their normal job (wrong
      // password, or nobody signed in yet on the startup call to /me).
      // Redirecting on those would fight with the login page itself.
      const isAuthEndpoint = req.url.startsWith(`${API_BASE_URL}/auth/`);

      if (error.status === 401 && isOurApi && !isAuthEndpoint) {
        auth.clearSession();
        router.navigate(['/login'], { queryParams: { redirectTo: router.url } });
      }

      // Re-throw so the component that made the call can still react.
      return throwError(() => error);
    }),
  );
};
