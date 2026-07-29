import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// The Author shape still lives next to Book; when authors grow their own
// feature it can move here without touching the callers.
import { Author } from '../books/book';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class AuthorService {
  private readonly apiUrl = 'http://localhost:8080/api/authors';

  private readonly http = inject(HttpClient);

  // GET /api/authors -> every author registered in the application
  getAuthors(): Observable<Author[]> {
    return this.http.get<Author[]>(this.apiUrl);
  }

  // GET /api/authors/{id} -> a single author
  getAuthor(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.apiUrl}/${id}`);
  }
}
