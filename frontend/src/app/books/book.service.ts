import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book } from './book';

// @Injectable marks this class as a service Angular can inject into components.
// "providedIn: 'root'" means there is ONE shared instance for the whole app.
@Injectable({ providedIn: 'root' })
export class BookService {
  // Base URL of your Spring Boot API. Angular talks to it over HTTP.
  private readonly apiUrl = 'http://localhost:8080/api/books';

  // Angular gives us (injects) an HttpClient to perform HTTP requests.
  private readonly http = inject(HttpClient);

  // GET /api/books -> the full list of books
  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(this.apiUrl);
  }

  // GET /api/books/{id} -> a single book
  getBook(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  // POST /api/books -> create a new book
  createBook(book: Book): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book);
  }

  // PUT /api/books/{id} -> update an existing book
  updateBook(id: number, book: Book): Observable<Book> {
    return this.http.put<Book>(`${this.apiUrl}/${id}`, book);
  }

  // DELETE /api/books/{id} -> remove a book
  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
