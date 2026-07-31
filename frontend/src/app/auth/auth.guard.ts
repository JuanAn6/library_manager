import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, RoleName } from './auth.service';

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

/**
 * Restricts a route to the given roles. Use it next to authGuard, which is the
 * one that deals with "nobody is signed in":
 *
 *   canActivate: [authGuard, roleGuard('ADMIN')]
 *
 * Same caveat as authGuard: this decides what the browser renders, nothing
 * more. The backend rejects the request on its own if the role is wrong.
 */
export const roleGuard = (...roles: RoleName[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const role = auth.user()?.role;
    // Send them somewhere they are allowed to be instead of a dead end.
    return !!role && roles.includes(role) ? true : router.createUrlTree(['/books']);
  };
};

/** The mirror image: keeps a signed-in user away from /login and /register. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isLoggedIn() ? router.createUrlTree(['/books']) : true;
};
