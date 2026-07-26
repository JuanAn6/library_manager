import { Component, signal } from '@angular/core';
import { BookList } from './books/book-list/book-list';

@Component({
  selector: 'app-root',
  imports: [BookList], // makes <app-book-list> usable inside this component's template
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  // A "signal" is a reactive box that holds a value.
  // In the template you READ it by calling it like a function: title()
  protected readonly title = signal('My Library');

  // Plain properties (no signal) also work in templates
  protected readonly tagline = 'Manage books, authors and loans';
  protected readonly year = new Date().getFullYear();

  // Another signal, this time a number, to demo interactivity
  protected readonly clicks = signal(0);

  // A method the template can call when the button is clicked
  addClick(): void {
    // update() reads the current value and returns the new one
    this.clicks.update((n) => n + 1);
  }
}
