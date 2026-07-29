import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BookService } from '../book.service';
import { AuthorService } from '../../authors/author.service';
import { CategoryService } from '../../categories/category.service';
import { PublisherService } from '../../publishers/publisher.service';
import { Author, Book, Category, Publisher } from '../book';

@Component({
  selector: 'app-book-edit',
  // ReactiveFormsModule gives the template [formGroup] and formControlName
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './book-edit.html',
  styleUrl: './book-edit.css',
})
export class BookEdit implements OnInit {
  // Ask Angular for the shared BookService instance
  private readonly bookService = inject(BookService);

  // These three feed the relation dropdowns with everything the app knows about
  private readonly authorService = inject(AuthorService);
  private readonly categoryService = inject(CategoryService);
  private readonly publisherService = inject(PublisherService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Router lets us navigate from TypeScript (routerLink is for templates)
  private readonly router = inject(Router);

  // FormBuilder is a small helper to build forms with less typing
  private readonly fb = inject(FormBuilder);

  // Reactive state the template can read
  protected readonly book = signal<Book | null>(null);
  protected readonly authors = signal<Author[]>([]);
  protected readonly categories = signal<Category[]>([]);
  protected readonly publishers = signal<Publisher[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  // The form STRUCTURE, declared up front and empty. The values arrive later,
  // when the API responds (see patchValue in ngOnInit).
  // "nonNullable" means a control resets to its initial value, never to null.
  // Each entry is [initialValue, validators].
  protected readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    isbn: [''],
    publishedDate: [''],
    language: [''],
    pageCount: [0, Validators.min(0)],
    location: [''],
    totalCopies: [0, [Validators.required, Validators.min(0)]],
    availableCopies: [0, [Validators.required, Validators.min(0)]],
    description: [''],
    // The selects hold ids, not whole entities: an id is a stable, comparable
    // value, while two objects with the same data are never equal.
    authorIds: [[] as number[]],
    // Category and publisher are optional in the model, so null is a valid
    // choice here: it is the "—" option at the top of each dropdown.
    categoryId: [null as number | null],
    publisherId: [null as number | null],
  });

  ngOnInit(): void {
    // snapshot = the value of the route at this exact moment.
    // Params always arrive as text, so we convert it to a number.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    // forkJoin waits for ALL the requests and emits once, so every dropdown
    // already has its options when we select the values of this book.
    forkJoin({
      book: this.bookService.getBook(id),
      authors: this.authorService.getAuthors(),
      categories: this.categoryService.getCategories(),
      publishers: this.publisherService.getPublishers(),
    }).subscribe({
      next: ({ book, authors, categories, publishers }) => {
        this.book.set(book); // keep the original: we need it again when saving
        // Sorted by name so the dropdowns are easy to scan
        this.authors.set(
          [...authors].sort((a, b) => this.authorName(a).localeCompare(this.authorName(b))),
        );
        this.categories.set([...categories].sort((a, b) => a.name.localeCompare(b.name)));
        this.publishers.set([...publishers].sort((a, b) => a.name.localeCompare(b.name)));
        this.loading.set(false);

        // THIS is what populates the inputs. patchValue copies the values into
        // the controls that match by name and ignores anything else.
        // Optional fields can be undefined/null, and the controls are
        // nonNullable, so we fall back with ?? to a valid empty value.
        this.form.patchValue({
          title: book.title,
          isbn: book.isbn ?? '',
          publishedDate: book.publishedDate ?? '',
          language: book.language ?? '',
          pageCount: book.pageCount ?? 0,
          location: book.location ?? '',
          totalCopies: book.totalCopies,
          availableCopies: book.availableCopies,
          description: book.description ?? '',
          authorIds: (book.authors ?? []).map((author) => author.id),
          categoryId: book.category?.id ?? null,
          publisherId: book.publisher?.id ?? null,
        });
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load book.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }

  // Label shown in the dropdown and in the summary line below it
  protected authorName(author: Author): string {
    return `${author.firstName} ${author.lastName}`.trim();
  }

  // The authors currently ticked in the dropdown, as full objects
  protected selectedAuthors(): Author[] {
    const ids = this.form.controls.authorIds.value;
    return this.authors().filter((author) => ids.includes(author.id));
  }

  // Same list as a single readable line, for the summary under the dropdown
  protected selectedAuthorNames(): string {
    return this.selectedAuthors()
      .map((author) => this.authorName(author))
      .join(', ');
  }

  // The category currently picked, or null when "—" is selected
  protected selectedCategory(): Category | null {
    const id = this.form.controls.categoryId.value;
    return this.categories().find((category) => category.id === id) ?? null;
  }

  // The publisher currently picked, or null when "—" is selected
  protected selectedPublisher(): Publisher | null {
    const id = this.form.controls.publisherId.value;
    return this.publishers().find((publisher) => publisher.id === id) ?? null;
  }

  save(): void {
    const original = this.book();

    // Guard: never send an invalid form. markAllAsTouched makes the error
    // messages show up (they are hidden until a field has been touched).
    if (!original || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // The *Id controls only exist in the form: the API expects real objects,
    // so we swap them for the matching entries of the lists we loaded.
    const { authorIds, categoryId, publisherId, ...values } = this.form.getRawValue();

    // The API's PUT replaces EVERY field, so we start from the original book
    // and let the form values override the ones we edited.
    const updated: Book = {
      ...original,
      ...values,
      authors: this.selectedAuthors(),
      category: this.selectedCategory(),
      publisher: this.selectedPublisher(),
    };

    this.saving.set(true);
    this.error.set(null);

    this.bookService.updateBook(original.id, updated).subscribe({
      next: () => {
        this.saving.set(false);
        // Back to the detail page of the book we just saved
        this.router.navigate(['/books', original.id]);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't save book.");
        this.saving.set(false);
        console.error(err);
      },
    });
  }
}
