import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../api';
import { User, UserCreate, UserUpdate } from './user';

// One shared instance for the whole app, same as BookService.
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = `${API_BASE_URL}/users`;

  private readonly http = inject(HttpClient);

  /**
   * GET /api/users -> every account that can sign in.
   *
   * With a search term the backend filters instead, matching any part of the
   * username or the email and ignoring capitals. Filtering there rather than in
   * the browser keeps the list correct once there are more accounts than one
   * response.
   */
  getUsers(search?: string): Observable<User[]> {
    // HttpParams builds the query string and escapes the value for us.
    const params = search ? new HttpParams().set('search', search) : undefined;
    return this.http.get<User[]>(this.apiUrl, { params });
  }

  // GET /api/users/{id} -> a single account
  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  // POST /api/users -> create an account, answered with the stored row (that is
  // where the id comes from, so we can go straight to its page)
  createUser(user: UserCreate): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  // PUT /api/users/{id} -> update an existing account
  updateUser(id: number, user: UserUpdate): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }
}
