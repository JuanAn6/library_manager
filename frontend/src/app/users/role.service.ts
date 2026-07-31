import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../api';
import { Role } from './user';

// One shared instance for the whole app, same as UserService.
@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly apiUrl = `${API_BASE_URL}/roles`;

  private readonly http = inject(HttpClient);

  // GET /api/roles -> every role an account can hold
  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(this.apiUrl);
  }
}
