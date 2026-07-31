import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { NAV_ITEMS, NavItem } from '../nav-items';

/**
 * The frame every signed-in page is rendered inside: a permanent side menu on
 * the left and the routed component on the right.
 *
 * It is wired as a parent route in app.routes.ts, so its <router-outlet>
 * displays whichever child route matches the URL.
 */
@Component({
  selector: 'app-base-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './base-layout.html',
  styleUrl: './base-layout.css',
})
export class BaseLayout {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly title = 'My Library';
  protected readonly year = new Date().getFullYear();

  protected readonly user = this.auth.user;

  /** On narrow screens the menu is hidden until the user opens it. */
  protected readonly menuOpen = signal(false);

  /**
   * The menu entries the current role is allowed to see. It is a computed
   * signal, so the sidebar updates on its own when the session changes.
   */
  protected readonly navItems = computed<NavItem[]>(() => {
    const role = this.user()?.role;
    return NAV_ITEMS.filter((item) => !item.roles || (!!role && item.roles.includes(role)));
  });

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    // Navigate whether or not the call succeeds: the local session is gone
    // either way, and leaving the user on a page they can no longer load
    // would only produce a second error.
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
