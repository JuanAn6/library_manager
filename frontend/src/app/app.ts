import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * The application shell. It holds nothing but the top level router outlet:
 * the visible frame (side menu, header, footer) lives in BaseLayout, which
 * the router renders as the parent of every signed-in route.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
