import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookService } from '../book.service';
import { AuthService } from '../../auth/auth.service';
import { Book } from '../book';

@Component({
  selector: 'app-book-list',
  imports: [RouterLink], // needed because the template uses [routerLink]
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList implements OnInit {
  // Ask Angular for the shared BookService instance
  private readonly bookService = inject(BookService);

  // Whether the current role may change the catalogue (ADMIN or MANAGER)
  protected readonly canEdit = inject(AuthService).canEditCatalogue;

  // Reactive state the template can read
  protected readonly books = signal<Book[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  // What the search box currently holds. Kept here rather than read off the
  // input, so search() works the same whether it was a click or Enter.
  protected readonly search = signal('');

  // ngOnInit runs once, right after the component is created.
  // It is the usual place to load data.
  ngOnInit(): void {
    this.load();
  }

  // Runs on submit of the little search form
  protected onSearch(term: string): void {
    this.search.set(term);
    this.load();
  }

  protected clearSearch(): void {
    this.search.set('');
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);

    // An empty box means "no filter", so we send no parameter at all
    const term = this.search().trim();

    this.bookService.getBooks(term || undefined).subscribe({
      next: (data) => {
        this.books.set(data); // store the books we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load books.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
