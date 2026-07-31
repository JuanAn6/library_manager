import { Routes } from '@angular/router';
import { BookList } from './books/book-list/book-list';
import { BookDetail } from './books/book-detail/book-detail';
import { BookEdit } from './books/book-edit/book-edit';
import { UserList } from './users/user-list/user-list';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { BaseLayout } from './layout/base-layout/base-layout';
import { authGuard, guestGuard } from './auth/auth.guard';

// Each route maps a URL path to the component that should be displayed.
// Angular checks them top to bottom and uses the FIRST one that matches.
export const routes: Routes = [
  // Public routes, rendered WITHOUT the side menu: there is no session to
  // navigate yet. guestGuard bounces you to /books if you are already in,
  // so a signed-in user never sees a login form.
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },

  // Everything below is a CHILD of BaseLayout, so it is drawn inside the
  // layout's <router-outlet> and the side menu stays on screen while the
  // content changes. authGuard sits on the parent, so it protects the whole
  // group at once: it runs BEFORE the components are created, so an anonymous
  // visitor never triggers their API calls.
  //
  // To add a new section: add a child route here and an entry in NAV_ITEMS
  // (see layout/nav-items.ts).
  {
    path: '',
    component: BaseLayout,
    canActivate: [authGuard],
    children: [
      // '' is the empty path (the home page). We redirect it to /books.
      // pathMatch: 'full' means "only redirect when the WHOLE url is empty".
      { path: '', redirectTo: 'books', pathMatch: 'full' },

      // /books -> the list
      { path: 'books', component: BookList },

      // /books/7 -> the detail. ":id" is a PARAMETER: it matches any value
      // and Angular stores it under the name "id" so the component can read it.
      { path: 'books/:id', component: BookDetail },
      { path: 'books/edit/:id', component: BookEdit },

      // /users -> placeholder until user management is built
      { path: 'users', component: UserList },
    ],
  },

  // '**' is the wildcard: anything that matched nothing above.
  { path: '**', redirectTo: 'books' },
];
