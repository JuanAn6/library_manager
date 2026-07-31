// The "shape" of a Category as returned by the backend API (GET /api/categories).
// It used to live next to Book, because a book was the only place a category
// showed up. Now that categories have their own screens it belongs here, and
// books/book.ts re-exports it so the existing imports keep working.
// Fields marked with "?" are optional (they may be missing or null).
export interface Category {
  id: number;
  name: string;
  description?: string | null;
}

// What POST /api/categories and PUT /api/categories/{id} accept. It is NOT the
// same shape as Category: the id is given by the database on a create, and
// travels in the URL on an update. See CategoryController.apply().
export interface CategoryRequest {
  name: string;
  description: string | null;
}
