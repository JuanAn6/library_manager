import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Blocks a route when nobody is signed in and sends the user to /login,
 * remembering where they were headed so we can return them there afterwards.
 *
 * This is convenience, not security: the guard only decides which component
 * Angular renders. The data is protected by the backend, which rejects every
 * request without a valid cookie no matter what the browser believes.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }

  // Returning a UrlTree redirects instead of just refusing.
  return router.createUrlTree(['/login'], { queryParams: { redirectTo: state.url } });
};

/** The mirror image: keeps a signed-in user away from /login and /register. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isLoggedIn() ? router.createUrlTree(['/books']) : true;
};
