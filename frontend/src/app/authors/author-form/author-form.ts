import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthorService } from '../author.service';
import { Author, AuthorRequest } from '../author';

/**
 * One component for both /authors/new and /authors/edit/:id.
 *
 * The two screens differ only in where the initial values come from and which
 * request is sent on save, so splitting them would mean maintaining the same
 * form twice. Which mode we are in is decided once, in ngOnInit, by whether the
 * route carries an ":id".
 */
@Component({
  selector: 'app-author-form',
  // ReactiveFormsModule gives the template [formGroup] and formControlName
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './author-form.html',
  styleUrl: './author-form.css',
})
export class AuthorForm implements OnInit {
  // Ask Angular for the shared AuthorService instance
  private readonly authorService = inject(AuthorService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Router lets us navigate from TypeScript (routerLink is for templates)
  private readonly router = inject(Router);

  // FormBuilder is a small helper to build forms with less typing
  private readonly fb = inject(FormBuilder);

  // Reactive state the template can read
  protected readonly author = signal<Author | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  // Null while creating, the id of the row being edited otherwise. Everything
  // the template needs to know about the mode is derived from it.
  private readonly editedId = signal<number | null>(null);
  protected readonly isNew = computed(() => this.editedId() === null);

  // Where "Cancel" and the back link go: the author's own page when there is
  // one, the list when we are creating and there is nothing to go back to.
  protected readonly cancelLink = computed(() => {
    const id = this.editedId();
    return id === null ? ['/authors'] : ['/authors', id];
  });

  // The form STRUCTURE, declared up front and empty. When editing, the values
  // arrive later, once the API responds (see patchValue in ngOnInit).
  // "nonNullable" means a control resets to its initial value, never to null.
  // Each entry is [initialValue, validators].
  protected readonly form = this.fb.nonNullable.group({
    // Required here as well as in the backend, so the obvious mistake is caught
    // before a request goes out. The backend checks it again regardless.
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    nationality: [''],
    birthDate: [''],
    biography: [''],
  });

  ngOnInit(): void {
    // Params always arrive as text. On /authors/new there is no "id" at all,
    // and that absence is what puts us in create mode.
    const param = this.route.snapshot.paramMap.get('id');

    if (param === null) {
      this.loading.set(false); // nothing to fetch: the form starts empty
      return;
    }

    const id = Number(param);
    this.editedId.set(id);

    this.authorService.getAuthor(id).subscribe({
      next: (author) => {
        this.author.set(author); // keep the original: we need its id when saving
        this.loading.set(false);

        // THIS is what populates the inputs. patchValue copies the values into
        // the controls that match by name and ignores anything else.
        // The optional fields can be null, and the controls are nonNullable,
        // so we fall back with ?? to a valid empty value.
        this.form.patchValue({
          firstName: author.firstName,
          lastName: author.lastName,
          nationality: author.nationality ?? '',
          birthDate: author.birthDate ?? '',
          biography: author.biography ?? '',
        });
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load author.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }

  save(): void {
    // Guard: never send an invalid form. markAllAsTouched makes the error
    // messages show up (they are hidden until a field has been touched).
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const values = this.form.getRawValue();

    // An input gives back "" when it is left empty, but the API stores the
    // optional fields as null, so we translate here rather than saving blanks.
    const payload: AuthorRequest = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      nationality: values.nationality.trim() || null,
      birthDate: values.birthDate || null,
      biography: values.biography.trim() || null,
    };

    this.saving.set(true);
    this.error.set(null);

    const id = this.editedId();
    const request =
      id === null
        ? this.authorService.createAuthor(payload)
        : this.authorService.updateAuthor(id, payload);

    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
        // Straight to the detail page of the author we just saved. On a create
        // the id only exists in the response, which is why we use "saved".
        this.router.navigate(['/authors', saved.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        // 400 and 409 are the backend rejecting the data, and its message says
        // exactly why, so it is worth showing. Anything else is a problem on
        // our side and gets the generic wording.
        const message = err.error?.message;
        this.error.set(
          (err.status === 400 || err.status === 409) && message
            ? message
            : "Something went wrong. Can't save author.",
        );
        console.error(err);
      },
    });
  }
}
