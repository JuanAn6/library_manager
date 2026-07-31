import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../api';
import { Category, CategoryRequest } from './category';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly apiUrl = `${API_BASE_URL}/categories`;

  private readonly http = inject(HttpClient);

  /**
   * GET /api/categories -> every category registered in the application.
   *
   * With a name the backend filters instead, matching any part of it and
   * ignoring capitals. Filtering there rather than in the browser keeps the
   * list correct once there are more categories than one response.
   */
  getCategories(name?: string): Observable<Category[]> {
    // HttpParams builds the query string and escapes the value for us.
    const params = name ? new HttpParams().set('name', name) : undefined;
    return this.http.get<Category[]>(this.apiUrl, { params });
  }

  // GET /api/categories/{id} -> a single category
  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  // POST /api/categories -> create a new category, answered with the stored row
  // (that is where the id comes from, so we can go straight to its page)
  createCategory(category: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, category);
  }

  // PUT /api/categories/{id} -> update an existing category
  updateCategory(id: number, category: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, category);
  }
}
