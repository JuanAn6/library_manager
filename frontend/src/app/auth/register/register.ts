import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth.service';

/**
 * Group-level validator: it compares two controls, so it cannot live on either
 * of them. Returns null when everything is fine, or an error object otherwise.
 */
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmation = group.get('confirmation')?.value;
  return password === confirmation ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: '../auth-form.css',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  // The rules mirror the ones the backend enforces in AuthController. The
  // backend is the one that matters; these only save the user a round trip.
  protected readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3)]],
      // Validators.email is permissive on purpose, like the backend's check:
      // only sending a message proves an address exists.
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmation: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const { username, email, password } = this.form.getRawValue();

    this.auth.register({ username, email, password }).subscribe({
      // The backend signs the new account in, so there is no login step here
      next: () => this.router.navigateByUrl('/books'),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(this.messageFor(err.status));
      },
    });
  }

  private messageFor(status: number): string {
    switch (status) {
      // The backend does not say WHICH of the two clashed, so neither do we:
      // naming it would confirm to a stranger that an account uses that email.
      case 409:
        return 'That username or email is already registered.';
      case 400:
        return 'Username needs 3 characters, password needs 8, and the email must be valid.';
      default:
        return 'Could not create the account. Please try again.';
    }
  }
}
