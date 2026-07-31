// The "shape" of an Author as returned by the backend API (GET /api/authors).
// It used to live next to Book, because a book was the only place an author
// showed up. Now that authors have their own screens it belongs here, and
// books/book.ts re-exports it so the existing imports keep working.
// Fields marked with "?" are optional (they may be missing or null).
export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  nationality?: string | null;
  birthDate?: string | null; // dates arrive as text in JSON, e.g. "1927-03-06"
  biography?: string | null;
}

// What POST /api/authors and PUT /api/authors/{id} accept. It is NOT the same
// shape as Author: the id is given by the database on a create, and travels in
// the URL on an update. See AuthorController.apply().
export interface AuthorRequest {
  firstName: string;
  lastName: string;
  nationality: string | null;
  birthDate: string | null;
  biography: string | null;
}
