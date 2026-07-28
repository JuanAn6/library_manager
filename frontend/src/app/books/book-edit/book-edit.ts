import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookService } from '../book.service';
import { Book } from '../book';

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

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Router lets us navigate from TypeScript (routerLink is for templates)
  private readonly router = inject(Router);

  // FormBuilder is a small helper to build forms with less typing
  private readonly fb = inject(FormBuilder);

  // Reactive state the template can read
  protected readonly book = signal<Book | null>(null);
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
  });

  ngOnInit(): void {
    // snapshot = the value of the route at this exact moment.
    // Params always arrive as text, so we convert it to a number.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.bookService.getBook(id).subscribe({
      next: (data) => {
        this.book.set(data); // keep the original: we need it again when saving
        this.loading.set(false);

        // THIS is what populates the inputs. patchValue copies the values into
        // the controls that match by name and ignores anything else.
        // Optional fields can be undefined/null, and the controls are
        // nonNullable, so we fall back with ?? to a valid empty value.
        this.form.patchValue({
          title: data.title,
          isbn: data.isbn ?? '',
          publishedDate: data.publishedDate ?? '',
          language: data.language ?? '',
          pageCount: data.pageCount ?? 0,
          location: data.location ?? '',
          totalCopies: data.totalCopies,
          availableCopies: data.availableCopies,
          description: data.description ?? '',
        });
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load book.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }

  save(): void {
    const original = this.book();

    // Guard: never send an invalid form. markAllAsTouched makes the error
    // messages show up (they are hidden until a field has been touched).
    if (!original || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // The API's PUT replaces EVERY field, including authors, category and
    // publisher. Spreading the original book first keeps those untouched;
    // the form values then override only the fields we edited.
    const updated: Book = { ...original, ...this.form.getRawValue() };

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
