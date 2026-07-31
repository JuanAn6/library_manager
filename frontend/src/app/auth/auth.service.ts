import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import { API_BASE_URL } from '../api';

/** The role names the backend defines, in the roles table. */
export type RoleName = 'ADMIN' | 'MANAGER' | 'MEMBER';

// What the backend answers on /login, /register and /me
export interface AuthUser {
  username: string;
  role: RoleName;
}

// What the sign-in form sends. "identifier" rather than "username" because the
// backend accepts either the username or the email address.
export interface Credentials {
  identifier: string;
  password: string;
}

// Registering asks for more than signing in does: the email is mandatory and
// cannot be derived from anything else.
export interface Registration {
  username: string;
  email: string;
  password: string;
}

/**
 * Holds the session state for the whole app.
 *
 * The token itself is NEVER visible here: it lives in an HttpOnly cookie that
 * JavaScript cannot read, and the browser attaches it on its own. That is why
 * the only way to know whether we are signed in is to ask the backend
 * (loadSession) and remember the answer in a signal.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${API_BASE_URL}/auth`;
  private readonly http = inject(HttpClient);

  // Writable inside the service, read-only for everyone else
  private readonly currentUser = signal<AuthUser | null>(null);
  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);

  login(credentials: Credentials): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${this.apiUrl}/login`, credentials)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  // The backend signs the new account in straight away, so the response is the
  // same as a login and we can treat it the same way.
  register(registration: Registration): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${this.apiUrl}/register`, registration)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/logout`, {})
      .pipe(tap(() => this.currentUser.set(null)));
  }

  /**
   * Asks the backend who the current cookie belongs to. Runs once at startup
   * (see provideAppInitializer in app.config.ts) so the guard has a definite
   * answer before the first route is resolved. A 401 here is not an error, it
   * just means "nobody is signed in".
   */
  loadSession(): Observable<AuthUser | null> {
    return this.http.get<AuthUser>(`${this.apiUrl}/me`).pipe(
      tap((user) => this.currentUser.set(user)),
      catchError(() => {
        this.currentUser.set(null);
        return of(null);
      }),
    );
  }

  /** Drops the local state without calling the backend (used on a 401). */
  clearSession(): void {
    this.currentUser.set(null);
  }

  hasRole(role: RoleName): boolean {
    return this.currentUser()?.role === role;
  }

  /** ADMIN and MANAGER may change the catalogue; MEMBER can only read it. */
  readonly canEditCatalogue = computed(() => {
    const role = this.currentUser()?.role;
    return role === 'ADMIN' || role === 'MANAGER';
  });
}
