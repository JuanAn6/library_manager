import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet], // makes <router-outlet> usable inside this component's template
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // A "signal" is a reactive box that holds a value.
  // In the template you READ it by calling it like a function: title()
  protected readonly title = signal('My Library');

  // Plain properties (no signal) also work in templates
  protected readonly tagline = 'Manage books, authors and loans';
  protected readonly year = new Date().getFullYear();

  // Another signal, this time a number, to demo interactivity
  protected readonly clicks = signal(0);

  // Signals owned by the auth service: the header re-renders on sign in/out
  protected readonly user = this.auth.user;

  // A method the template can call when the button is clicked
  addClick(): void {
    // update() reads the current value and returns the new one
    this.clicks.update((n) => n + 1);
  }

  logout(): void {
    // navigate whether or not the call succeeds: the local session is gone
    // either way, and leaving the user on a page they can no longer load
    // would only produce a second error.
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
