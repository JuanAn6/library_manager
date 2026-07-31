import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { UserService } from '../user.service';
import { RoleService } from '../role.service';
import { AuthService, RoleName } from '../../auth/auth.service';
import { Role, User } from '../user';

/**
 * One component for both /users/new and /users/edit/:id.
 *
 * The two screens differ only in where the initial values come from and which
 * request is sent on save, so splitting them would mean maintaining the same
 * form twice. Which mode we are in is decided once, in ngOnInit, by whether the
 * route carries an ":id".
 */
@Component({
  selector: 'app-user-form',
  // ReactiveFormsModule gives the template [formGroup] and formControlName
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css',
})
export class UserForm implements OnInit {
  // Ask Angular for the shared UserService instance
  private readonly userService = inject(UserService);

  // Feeds the role dropdown with every role the backend knows about
  private readonly roleService = inject(RoleService);

  // Used to spot the account we are signed in as, see isSelf()
  private readonly auth = inject(AuthService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Router lets us navigate from TypeScript (routerLink is for templates)
  private readonly router = inject(Router);

  // FormBuilder is a small helper to build forms with less typing
  private readonly fb = inject(FormBuilder);

  // Reactive state the template can read
  protected readonly user = signal<User | null>(null);
  protected readonly roles = signal<Role[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  // True when an admin is editing their OWN account. The backend refuses to
  // let them drop their own role or disable themselves (it would lock them out
  // of this very screen), so the form greys those two fields out rather than
  // letting the user find out by getting an error back.
  protected readonly isSelf = signal(false);

  // Null while creating, the id of the row being edited otherwise. Everything
  // the template needs to know about the mode is derived from it.
  private readonly editedId = signal<number | null>(null);
  protected readonly isNew = computed(() => this.editedId() === null);

  // Where "Cancel" goes: the account's own page when there is one, the list
  // when we are creating and there is nothing to go back to.
  protected readonly cancelLink = computed(() => {
    const id = this.editedId();
    return id === null ? ['/users'] : ['/users', id];
  });

  // The form STRUCTURE, declared up front and empty. When editing, the values
  // arrive later, once the API responds (see patchValue in ngOnInit).
  // "nonNullable" means a control resets to its initial value, never to null.
  // Each entry is [initialValue, validators].
  protected readonly form = this.fb.nonNullable.group({
    // Same floor as the backend, so the obvious mistake is caught before a
    // request goes out. The backend checks it again regardless.
    username: ['', [Validators.required, Validators.minLength(3)]],
    // Mandatory: it is one of the two ways this account can sign in.
    email: ['', [Validators.required, Validators.email]],
    // The select holds the role NAME, which is what the API expects
    role: ['MEMBER' as RoleName, Validators.required],
    enabled: [true],
    // Only used when creating: a new account needs something to sign in with.
    // Editing an existing one never touches the password, so the control is
    // disabled below and stays out of the payload.
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit(): void {
    // Params always arrive as text. On /users/new there is no "id" at all, and
    // that absence is what puts us in create mode.
    const param = this.route.snapshot.paramMap.get('id');
    const id = param === null ? null : Number(param);
    this.editedId.set(id);

    if (id !== null) {
      // Changing somebody's password is a separate operation, not a side effect
      // of saving the rest of the form. A disabled control is also skipped by
      // the validators, so an empty one cannot block the submit.
      this.form.controls.password.disable();
    }

    // forkJoin waits for BOTH requests and emits once, so the dropdown already
    // has its options when we select the role of this user. When creating there
    // is no user to fetch, and of(null) fills that slot so the rest of this
    // method needs no second version.
    forkJoin({
      user: id === null ? of(null) : this.userService.getUser(id),
      roles: this.roleService.getRoles(),
    }).subscribe({
      next: ({ user, roles }) => {
        this.user.set(user); // keep the original: we need its id when saving
        this.roles.set(roles);
        this.loading.set(false);

        if (!user) {
          return; // creating: the form keeps the empty values declared above
        }

        // THIS is what populates the inputs. patchValue copies the values into
        // the controls that match by name and ignores anything else.
        this.form.patchValue({
          username: user.username,
          email: user.email,
          role: user.role,
          enabled: user.enabled,
        });

        if (user.username === this.auth.user()?.username) {
          this.isSelf.set(true);
          // A disabled control is still part of getRawValue(), so the payload
          // keeps sending the current role and status unchanged.
          this.form.controls.role.disable();
          this.form.controls.enabled.disable();
        }
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load user.");
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

    this.saving.set(true);
    this.error.set(null);

    // getRawValue() instead of value: it includes the controls we disabled for
    // an admin editing themselves, which the API still expects to receive.
    const { password, ...values } = this.form.getRawValue();

    const id = this.editedId();
    const request =
      id === null
        ? this.userService.createUser({ ...values, password })
        : this.userService.updateUser(id, values);

    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
        // Straight to the detail page of the account we just saved. On a create
        // the id only exists in the response, which is why we use "saved".
        this.router.navigate(['/users', saved.id]);
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
            : "Something went wrong. Can't save user.",
        );
        console.error(err);
      },
    });
  }
}
