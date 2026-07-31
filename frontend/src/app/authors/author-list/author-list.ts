import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthorService } from '../author.service';
import { AuthService } from '../../auth/auth.service';
import { Author } from '../author';

@Component({
  selector: 'app-author-list',
  imports: [RouterLink], // needed because the template uses [routerLink]
  templateUrl: './author-list.html',
  styleUrl: './author-list.css',
})
export class AuthorList implements OnInit {
  // Ask Angular for the shared AuthorService instance
  private readonly authorService = inject(AuthorService);

  // Whether the current role may change the catalogue (ADMIN or MANAGER).
  // Authors are part of the catalogue, so a MEMBER can read this list but not
  // the "New author" and "Edit" links.
  protected readonly canEdit = inject(AuthService).canEditCatalogue;

  // Reactive state the template can read
  protected readonly authors = signal<Author[]>([]);
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

    this.authorService.getAuthors(term || undefined).subscribe({
      next: (data) => {
        this.authors.set(data); // store the authors we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load authors.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
