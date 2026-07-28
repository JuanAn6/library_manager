import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookService } from '../book.service';
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

  // Reactive state the template can read
  protected readonly books = signal<Book[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  // ngOnInit runs once, right after the component is created.
  // It is the usual place to load data.
  ngOnInit(): void {
    this.bookService.getBooks().subscribe({
      next: (data) => {
        this.books.set(data); // store the books we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Something go wrong. Can\'t load books.');
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
