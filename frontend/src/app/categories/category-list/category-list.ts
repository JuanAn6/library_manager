import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../category.service';
import { AuthService } from '../../auth/auth.service';
import { Category } from '../category';

@Component({
  selector: 'app-category-list',
  imports: [RouterLink], // needed because the template uses [routerLink]
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
})
export class CategoryList implements OnInit {
  // Ask Angular for the shared CategoryService instance
  private readonly categoryService = inject(CategoryService);

  // Whether the current role may change the catalogue (ADMIN or MANAGER).
  // Categories are part of the catalogue, so a MEMBER can read this list but
  // not the "New category" and "Edit" links.
  protected readonly canEdit = inject(AuthService).canEditCatalogue;

  // Reactive state the template can read
  protected readonly categories = signal<Category[]>([]);
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

    this.categoryService.getCategories(term || undefined).subscribe({
      next: (data) => {
        this.categories.set(data); // store the categories we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load categories.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
