import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../api';
import { Author, AuthorRequest } from './author';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class AuthorService {
  private readonly apiUrl = `${API_BASE_URL}/authors`;

  private readonly http = inject(HttpClient);

  /**
   * GET /api/authors -> every author registered in the application.
   *
   * With a lastName the backend filters instead, matching any part of the
   * surname and ignoring capitals. Filtering there rather than in the browser
   * keeps the list correct once there are more authors than one response.
   */
  getAuthors(lastName?: string): Observable<Author[]> {
    // HttpParams builds the query string and escapes the value for us.
    const params = lastName ? new HttpParams().set('lastName', lastName) : undefined;
    return this.http.get<Author[]>(this.apiUrl, { params });
  }

  // GET /api/authors/{id} -> a single author
  getAuthor(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.apiUrl}/${id}`);
  }

  // POST /api/authors -> create a new author, answered with the stored row
  // (that is where the id comes from, so we can go straight to its page)
  createAuthor(author: AuthorRequest): Observable<Author> {
    return this.http.post<Author>(this.apiUrl, author);
  }

  // PUT /api/authors/{id} -> update an existing author
  updateAuthor(id: number, author: AuthorRequest): Observable<Author> {
    return this.http.put<Author>(`${this.apiUrl}/${id}`, author);
  }
}
