import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// The Publisher shape still lives next to Book; when publishers grow their own
// feature it can move here without touching the callers.
import { Publisher } from '../books/book';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class PublisherService {
  private readonly apiUrl = 'http://localhost:8080/api/publishers';

  private readonly http = inject(HttpClient);

  // GET /api/publishers -> every publisher registered in the application
  getPublishers(): Observable<Publisher[]> {
    return this.http.get<Publisher[]>(this.apiUrl);
  }

  // GET /api/publishers/{id} -> a single publisher
  getPublisher(id: number): Observable<Publisher> {
    return this.http.get<Publisher>(`${this.apiUrl}/${id}`);
  }
}
