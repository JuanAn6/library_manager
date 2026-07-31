import { RoleName } from '../auth/auth.service';

/**
 * One entry of the side menu.
 *
 * Adding a new section to the application is a two step job: register its
 * route in app.routes.ts as a child of the BaseLayout route, and push an
 * item here. Nothing else needs to change.
 */
export interface NavItem {
  /** Text shown in the sidebar. Must be in English (see CLAUDE.md). */
  label: string;
  /** Absolute route the item points at, e.g. '/books'. */
  path: string;
  /** Small glyph rendered before the label. */
  icon: string;
  /**
   * Roles allowed to see the item. When omitted the item is visible to every
   * signed-in user. This only hides the link: the route guards and the backend
   * are what actually protect the data.
   */
  roles?: RoleName[];
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Books', path: '/books', icon: '📚' },
  { label: 'Users', path: '/users', icon: '👥', roles: ['ADMIN'] },
];
