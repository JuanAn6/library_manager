import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// The Category shape still lives next to Book; when categories grow their own
// feature it can move here without touching the callers.
import { Category } from '../books/book';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly apiUrl = 'http://localhost:8080/api/categories';

  private readonly http = inject(HttpClient);

  // GET /api/categories -> every category registered in the application
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }

  // GET /api/categories/{id} -> a single category
  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }
}
