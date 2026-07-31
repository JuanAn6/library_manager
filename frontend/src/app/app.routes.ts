import { Routes } from '@angular/router';
import { BookList } from './books/book-list/book-list';
import { BookDetail } from './books/book-detail/book-detail';
import { BookForm } from './books/book-form/book-form';
import { AuthorList } from './authors/author-list/author-list';
import { AuthorDetail } from './authors/author-detail/author-detail';
import { AuthorForm } from './authors/author-form/author-form';
import { CategoryList } from './categories/category-list/category-list';
import { CategoryDetail } from './categories/category-detail/category-detail';
import { CategoryForm } from './categories/category-form/category-form';
import { UserList } from './users/user-list/user-list';
import { UserDetail } from './users/user-detail/user-detail';
import { UserForm } from './users/user-form/user-form';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { BaseLayout } from './layout/base-layout/base-layout';
import { authGuard, guestGuard, roleGuard } from './auth/auth.guard';

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

      // 'books/new' MUST come before 'books/:id': routes are matched top to
      // bottom, and ":id" would happily swallow the word "new" and try to load
      // a book with that id. Creating and editing share one component, see
      // BookForm.
      {
        path: 'books/new',
        component: BookForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },

      // /books/7 -> the detail. ":id" is a PARAMETER: it matches any value
      // and Angular stores it under the name "id" so the component can read it.
      { path: 'books/:id', component: BookDetail },
      {
        path: 'books/edit/:id',
        component: BookForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },

      // /authors -> the list. Reading is open to every signed-in user, the same
      // as the books it belongs to; the two write screens are staff only,
      // matching the backend rule in SecurityConfig.
      { path: 'authors', component: AuthorList },

      // 'authors/new' MUST come before 'authors/:id': routes are matched top to
      // bottom, and ":id" would happily swallow the word "new" and try to load
      // an author with that id.
      {
        path: 'authors/new',
        component: AuthorForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },
      { path: 'authors/:id', component: AuthorDetail },
      {
        path: 'authors/edit/:id',
        component: AuthorForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },

      // /categories -> the list. Same rules as authors: every signed-in user
      // may read the catalogue, only staff may change it.
      { path: 'categories', component: CategoryList },

      // 'categories/new' MUST come before 'categories/:id': routes are matched
      // top to bottom, and ":id" would happily swallow the word "new" and try
      // to load a category with that id.
      {
        path: 'categories/new',
        component: CategoryForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },
      { path: 'categories/:id', component: CategoryDetail },
      {
        path: 'categories/edit/:id',
        component: CategoryForm,
        canActivate: [roleGuard('ADMIN', 'MANAGER')],
      },

      // /users -> the account list. ADMIN only, matching both the sidebar
      // entry (see NAV_ITEMS) and the backend rule in SecurityConfig.
      // The guard is repeated on each route: a guard on a route does NOT apply
      // to its siblings, and /users/7 has to be as protected as /users.
      { path: 'users', component: UserList, canActivate: [roleGuard('ADMIN')] },
      // Same ordering rule as 'books/new' above: this line has to come before
      // 'users/:id'. Creating and editing share one component, see UserForm.
      { path: 'users/new', component: UserForm, canActivate: [roleGuard('ADMIN')] },
      { path: 'users/:id', component: UserDetail, canActivate: [roleGuard('ADMIN')] },
      { path: 'users/edit/:id', component: UserForm, canActivate: [roleGuard('ADMIN')] },
    ],
  },

  // '**' is the wildcard: anything that matched nothing above.
  { path: '**', redirectTo: 'books' },
];
