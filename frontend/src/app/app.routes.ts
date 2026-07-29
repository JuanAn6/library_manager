import { Routes } from '@angular/router';
import { BookList } from './books/book-list/book-list';
import { BookDetail } from './books/book-detail/book-detail';
import { BookEdit } from './books/book-edit/book-edit';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { authGuard, guestGuard } from './auth/auth.guard';

// Each route maps a URL path to the component that should be displayed.
// Angular checks them top to bottom and uses the FIRST one that matches.
export const routes: Routes = [
  // '' is the empty path (the home page). We redirect it to /books.
  // pathMatch: 'full' means "only redirect when the WHOLE url is empty".
  { path: '', redirectTo: 'books', pathMatch: 'full' },

  // Public routes. guestGuard bounces you to /books if you are already in,
  // so a signed-in user never sees a login form.
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },

  // Everything below needs a session. canActivate runs BEFORE the component
  // is created, so an anonymous visitor never triggers its API calls.
  // /books -> the list
  { path: 'books', component: BookList, canActivate: [authGuard] },

  // /books/7 -> the detail. ":id" is a PARAMETER: it matches any value
  // and Angular stores it under the name "id" so the component can read it.
  { path: 'books/:id', component: BookDetail, canActivate: [authGuard] },
  { path: 'books/edit/:id', component: BookEdit, canActivate: [authGuard] },

  // '**' is the wildcard: anything that matched nothing above.
  { path: '**', redirectTo: 'books' },
];
