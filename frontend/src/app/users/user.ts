import { RoleName } from '../auth/auth.service';

// The "shape" of a User as returned by the backend API (GET /api/users).
// It mirrors UserController.UserSummary: the password hash is never part of
// a response, so there is nothing like it here either.
export interface User {
  id: number;
  username: string;
  email: string; // mandatory, and a second way to sign in
  role: RoleName;
  enabled: boolean;
  createdAt?: string; // dates arrive as text in JSON, e.g. "2024-05-01T10:15:30"
}

// What PUT /api/users/{id} accepts. It is NOT the same shape as User: the id
// travels in the URL, createdAt is not editable, and the password belongs to
// its own operation. See UserController.UserUpdateRequest.
export interface UserUpdate {
  username: string;
  email: string;
  role: RoleName;
  enabled: boolean;
}

// What POST /api/users accepts. Same as UserUpdate plus the password: a brand
// new account has to be able to sign in, and there is nothing to keep.
// See UserController.UserCreateRequest.
export interface UserCreate extends UserUpdate {
  password: string;
}

// A role as returned by GET /api/roles. Used to fill the dropdown of the edit
// form, the same way authors and categories fill the ones on the book form.
export interface Role {
  id: number;
  name: RoleName;
}
