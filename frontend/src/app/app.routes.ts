import { Routes } from '@angular/router';
import { BookList } from './books/book-list/book-list';
import { BookDetail } from './books/book-detail/book-detail';
import { BookEdit } from './books/book-edit/book-edit';

// Each route maps a URL path to the component that should be displayed.
// Angular checks them top to bottom and uses the FIRST one that matches.
export const routes: Routes = [
  // '' is the empty path (the home page). We redirect it to /books.
  // pathMatch: 'full' means "only redirect when the WHOLE url is empty".
  { path: '', redirectTo: 'books', pathMatch: 'full' },

  // /books -> the list
  { path: 'books', component: BookList },

  // /books/7 -> the detail. ":id" is a PARAMETER: it matches any value
  // and Angular stores it under the name "id" so the component can read it.
  { path: 'books/:id', component: BookDetail },
  { path: 'books/edit/:id', component: BookEdit },

  // '**' is the wildcard: anything that matched nothing above.
  { path: '**', redirectTo: 'books' },
];
