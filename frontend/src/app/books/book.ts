// The "shape" of a Book as returned by the backend API (GET /api/books).
// TypeScript uses this interface to check our code and give us autocomplete.
// Fields marked with "?" are optional (they may be missing or null).
export interface Book {
  id: number;
  title: string;
  isbn?: string;
  authors: Author[];
  category?: Category | null;
  publisher?: Publisher | null;
  language?: string;
  pageCount?: number;
  description?: string;
  coverImageUrl?: string;
  publishedDate?: string; // dates arrive as text in JSON, e.g. "2024-05-01"
  totalCopies: number;
  availableCopies: number;
  location?: string;
  available: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Nested types used inside a Book. For now they live here; later they can
// move to their own feature folders (authors/, categories/, publishers/).
export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  nationality?: string;
  birthDate?: string;
  biography?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface Publisher {
  id: number;
  name: string;
  country?: string;
  website?: string;
}
