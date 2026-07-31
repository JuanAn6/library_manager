import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { UserService } from '../user.service';
import { User } from '../user';

@Component({
  selector: 'app-user-detail',
  imports: [RouterLink, DatePipe], // the template uses routerLink and the date pipe
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetail implements OnInit {
  // Ask Angular for the shared UserService instance
  private readonly userService = inject(UserService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Reactive state the template can read
  protected readonly user = signal<User | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  // No canEdit here: the whole /users section is ADMIN only (see roleGuard in
  // app.routes.ts), so anyone who can see this page can also edit.

  ngOnInit(): void {
    // snapshot = the value of the route at this exact moment.
    // Params always arrive as text, so we convert it to a number.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.userService.getUser(id).subscribe({
      next: (data) => {
        this.user.set(data); // store the user we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load user.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
