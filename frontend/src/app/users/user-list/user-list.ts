import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { UserService } from '../user.service';
import { User } from '../user';

@Component({
  selector: 'app-user-list',
  // routerLink for the links, DatePipe for the "date" pipe in the template
  imports: [RouterLink, DatePipe],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
})
export class UserList implements OnInit {
  // Ask Angular for the shared UserService instance
  private readonly userService = inject(UserService);

  // Reactive state the template can read
  protected readonly users = signal<User[]>([]);
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

    this.userService.getUsers(term || undefined).subscribe({
      next: (data) => {
        this.users.set(data); // store the users we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load users.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
