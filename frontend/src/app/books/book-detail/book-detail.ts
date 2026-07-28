import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../book.service';
import { Book } from '../book';

@Component({
  selector: 'app-book-detail',
  imports: [RouterLink], // the template uses routerLink for the "back" link
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.css',
})
export class BookDetail implements OnInit {
  // Ask Angular for the shared BookService instance
  private readonly bookService = inject(BookService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Reactive state the template can read
  protected readonly book = signal<Book | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  // ngOnInit runs once, right after the component is created.
  // It is the usual place to load data.
  ngOnInit(): void {
    // snapshot = the value of the route at this exact moment.
    // Params always arrive as text, so we convert it to a number.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.bookService.getBook(id).subscribe({
      next: (data) => {
        this.book.set(data); // store the book we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load book.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
