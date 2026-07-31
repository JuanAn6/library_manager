import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthorService } from '../author.service';
import { AuthService } from '../../auth/auth.service';
import { Author } from '../author';

@Component({
  selector: 'app-author-detail',
  imports: [RouterLink], // the template uses routerLink for the "back" link
  templateUrl: './author-detail.html',
  styleUrl: './author-detail.css',
})
export class AuthorDetail implements OnInit {
  // Ask Angular for the shared AuthorService instance
  private readonly authorService = inject(AuthorService);

  // ActivatedRoute describes the route that is currently displayed,
  // including its parameters (the ":id" we declared in app.routes.ts).
  private readonly route = inject(ActivatedRoute);

  // Whether the current role may change the catalogue (ADMIN or MANAGER)
  protected readonly canEdit = inject(AuthService).canEditCatalogue;

  // Reactive state the template can read
  protected readonly author = signal<Author | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  // ngOnInit runs once, right after the component is created.
  // It is the usual place to load data.
  ngOnInit(): void {
    // snapshot = the value of the route at this exact moment.
    // Params always arrive as text, so we convert it to a number.
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.authorService.getAuthor(id).subscribe({
      next: (data) => {
        this.author.set(data); // store the author we received
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set("Something went wrong. Can't load author.");
        this.loading.set(false);
        console.error(err);
      },
    });
  }
}
