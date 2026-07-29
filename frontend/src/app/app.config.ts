import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './auth/auth.interceptor';
import { AuthService } from './auth/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Enables HttpClient app-wide, so services like BookService can make
    // requests. The interceptor runs on every one of them.
    provideHttpClient(withInterceptors([authInterceptor])),
    // Runs before the app is shown. Angular waits for the returned Observable,
    // so by the time the router resolves the first route we already know
    // whether the cookie we may be carrying is a valid session.
    provideAppInitializer(() => inject(AuthService).loadSession()),
  ],
};
