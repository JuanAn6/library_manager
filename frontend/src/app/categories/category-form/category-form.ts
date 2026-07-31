import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CategoryService } from '../category.service';
import { Category, CategoryRequest } from '../category';

/**
 * One component for both /categories/new and /categories/edit/:id.
 *
 * The two screens differ only in where the initial values come from and which
 * request is sent on save, so splitting them would mean maintaining the same
 * form twice. Which mode we are in is decided once, in ngOnInit, by whether the
 * route carries an ":id".
 */
@Component({
  selector: 'app-category-form',
  // ReactiveFormsModule gives the template [formGroup] and formControlName
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './category-form.html',
  styleUrl: './category-form.css',
})
export class CategoryForm implements OnInit {
  // Ask Angular for the shared CategoryService instance
  private readonly categoryService = inject(CategoryService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Router lets us navigate from TypeScript (routerLink is for templates)
  private readonly router = inject(Router);

  // FormBuilder is a small helper to build forms with less typing
  private readonly fb = inject(FormBuilder);

  // Reactive state the template can read
  protected readonly category = signal<Category | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  // Null while creating, the id of the row being edited otherwise. Everything
  // the template needs to know about the mode is derived from it.
  private readonly editedId = signal<number | null>(null);
  protected readonly isNew = computed(() => this.editedId() === null);

  // Where "Cancel" and the back link go: the category's own page when there is
  // one, the list when we are creating and there is nothing to go back to.
  protected readonly cancelLink = computed(() => {
    const id = this.editedId();
    return id === null ? ['/categories'] : ['/categories', id];
  });

  // The form STRUCTURE, declared up front and empty. When editing, the values
  // arrive later, once the API responds (see patchValue in ngOnInit).
  // "nonNullable" means a control resets to its initial value, never to null.
  // Each entry is [initialValue, validators].
  protected readonly form = this.fb.nonNullable.group({
    // The lengths mirror the columns the backend validates against, so the
    // obvious mistake is caught before a request goes out. The backend checks
    // them again regardless.
    name: ['', [Validators.required, Validators.maxLength(80)]],
    description: ['', Validators.maxLength(500)],
  });

  ngOnInit(): void {
    // Params always arrive as text. On /categories/new there is no "id" at all,
    // and that absence is what puts us in create mode.
    const param = this.route.snapshot.paramMap.get('id');

    if (param === null) {
      this.loading.set(false); // nothing to fetch: the form starts empty
      return;
    }

    const id = Number(param);
    this.editedId.set(id);

    this.categoryService.getCategory(id).subscribe({
      next: (category) => {
        this.category.set(category); // keep the original: we need its id when saving
        this.loading.set(false);

        // THIS is what populates the inputs. patchValue copies the values into
        // the controls that match by name and ignores anything else.
        // The description can be null, and the controls are nonNullable, so we
        // fall back with ?? to a valid empty value.
        this.form.patchValue({
          name: category.name,
          description: category.description ?? '',
        });
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load category.");
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
    // description as null, so we translate here rather than saving a blank.
    const payload: CategoryRequest = {
      name: values.name.trim(),
      description: values.description.trim() || null,
    };

    this.saving.set(true);
    this.error.set(null);

    const id = this.editedId();
    const request =
      id === null
        ? this.categoryService.createCategory(payload)
        : this.categoryService.updateCategory(id, payload);

    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
        // Straight to the detail page of the category we just saved. On a
        // create the id only exists in the response, which is why we use
        // "saved".
        this.router.navigate(['/categories', saved.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        // 400 and 409 are the backend rejecting the data, and its message says
        // exactly why (a duplicate name, most often), so it is worth showing.
        // Anything else is a problem on our side and gets the generic wording.
        const message = err.error?.message;
        this.error.set(
          (err.status === 400 || err.status === 409) && message
            ? message
            : "Something went wrong. Can't save category.",
        );
        console.error(err);
      },
    });
  }
}
