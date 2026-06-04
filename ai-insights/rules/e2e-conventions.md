---
description: Test conventions (unit, API, E2E) — applied when editing test files
globs: tests/e2e/**/*.ts, tests/setup/**/*.ts, tests/api/**/*.ts, tests/unit/**/*.ts, tests/accessibility/**/*.ts
author: Renata Andrade
---

# Test Conventions

## Test Pyramid — Always Apply

Before writing or modifying any test, verify it is at the **lowest viable level**:

1. **Unit** (`tests/unit/`) — Validation schemas, pure functions, helpers. Use Vitest. Fastest, cheapest, preferred.
2. **API** (`tests/api/`) — Route handler behavior: CRUD, auth, error responses, status codes. Use Playwright `APIRequestContext`. No browser needed.
3. **E2E** (`tests/e2e/specs/`) — Value comes from the browser: rendering, navigation, multi-step interaction, runtime errors. Use Playwright browser. Most expensive, use sparingly.

**If the same behavior can be verified at a lower level, it MUST be tested there instead of E2E.**

For read-only data-display pages, a single E2E smoke test is sufficient (navigate → heading visible → no network/console errors).

## Locators

- **Always use semantic locators**: `getByRole()`, `getByLabel()`, `getByText()`, `getByPlaceholder()`, `getByTestId()`
- **Never use XPath** (`xpath=...`) or complex CSS selectors (`[data-slot='card']`, `table tbody tr td a`)
- **Use `.filter()` to narrow scope** instead of chaining CSS selectors
- **No locators in spec files** — all element interaction must go through Page Object Models in `tests/e2e/pages/*.page.ts`
- If no good locator exists, **fix the UI source** — add `aria-label`, `role`, or `data-testid` to the component

### Locator Priority
1. `getByRole()` — always preferred (also improves a11y)
2. `getByLabel()` — for form fields
3. `getByText()` — for content verification
4. `getByPlaceholder()` — for inputs without labels
5. `getByTestId()` — last resort when no semantic locator is possible

### Locator Placement in Page Object Models

All reusable locators in POM files (`tests/e2e/pages/*.page.ts`) must be declared as `private readonly` class fields and assigned in the constructor. Methods reference them via `this.*`.

**Exceptions** (may remain inline in methods):
- Parameterized locators where the selector depends on a method argument (e.g., `clickSeries(name: string)`)
- One-off chained/filtered locators derived from a constructor locator that are only used inside a single method

```typescript
// GOOD — locators assigned in constructor, methods use this.*
export class LoginPage {
  private readonly page: Page;
  private readonly emailInput: Locator;
  private readonly signInButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.getByLabel("Email");
    this.signInButton = page.getByRole("button", { name: "Sign In" });
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.signInButton.click();
  }
}

// BAD — inline locators in methods
async login(email: string, password: string) {
  await this.page.getByLabel("Email").fill(email);
  await this.page.getByRole("button", { name: "Sign In" }).click();
}
```

## Test Structure

- **One `test.describe()` per spec file** — every spec must have exactly one top-level `test.describe()` with all tests inside it. No multiple top-level describes, no tests outside a describe.
- **`test.describe()` naming convention** — the describe label MUST start with a prefix matching the test type:
  - `"E2E: ..."` for E2E specs in `tests/e2e/specs/`
  - `"API: ..."` for API specs in `tests/api/specs/`
  - `"A11Y: ..."` for accessibility specs in `tests/accessibility/`
  - `"Setup: ..."` for setup files in `tests/setup/`
- **Hooks live outside the describe** — `test.beforeAll`, `test.beforeEach`, `test.afterAll`, `test.afterEach` must be declared **above** the `test.describe()` block, never inside it. At most one of each per file, in this order:
  1. `test.beforeAll`
  2. `test.beforeEach`
  3. `test.afterAll`
  4. `test.afterEach`
- **No `test.skip`** — tests must fail if prerequisites are missing, not silently skip
- **No `waitForTimeout`** — use auto-retrying `expect()` or `page.waitForLoadState()`
- **No generic spec files** (e.g., `bugfix-regression.spec.ts`) — tests belong in domain-specific files
- **Use `test.step()`** for readable test structure
- **Import from `../fixtures`** — never from `@playwright/test` directly in E2E spec files

## Authentication

- Admin auth is handled via `storageState` from the setup project — **never log in manually** in admin tests
- Auth spec tests use `test.use({ storageState: { cookies: [], origins: [] } })` to opt out
- Login form testing is only in `auth.spec.ts`

## No Direct Database Access

- **Tests must never connect to the database directly** — no Prisma imports, no raw SQL, no `mysql2`
- All data the setup needs (user IDs, emails, roles) must come from **environment variables**
- If a test needs to verify data state, use the **API request helpers** (`tests/api/requests/*.api.ts`) through the running application
- This keeps tests decoupled from the data layer and avoids ESM/CJS incompatibilities with Prisma

## Fixtures

- Split by context: `admin.fixtures.ts`, `learner.fixtures.ts`, `common.fixtures.ts`
- Index re-exports merged fixture
- Page objects are instantiated in fixtures, not in specs

## URLs and Routes

- **Import from `src/lib/routes.ts`** — never hardcode URL paths
- Use `ROUTES.*` for page URLs
- Use `API.*` for API endpoints

## Playwright Projects

Tests are split across Playwright projects so each type runs in the right context:

| Project | What runs | Browser |
|---|---|---|
| `setup` | `tests/setup/*.setup.ts` | none |
| `api` | `tests/api/specs/*.spec.ts` | none (API only) |
| `mobile` | `tests/e2e/specs/` + `tests/accessibility/` | iPhone 14 |
| `tablet` | same | iPad (gen 7) |
| `desktop` | same | Desktop Chrome 1440×900 |

API tests run once without a browser. E2E and A11Y tests run in all three viewports.

## API Testing

- API request helpers live in `tests/api/requests/*.api.ts`
- API spec files live in `tests/api/specs/*.spec.ts`
- Request helpers accept `APIRequestContext` and return raw responses
- Spec files import from `../requests/` — never hardcode API paths in specs
- Do not mix API assertions into UI tests — prefer asserting visible UI elements

## No Interfaces in Test Files

- **Do not define interfaces** in spec files, page object models, fixtures, setup files, or API request helpers.
- If a test needs a type, define it in the source code in a `*-types.ts` file and import it.
- See `.cursor/rules/interface-conventions.mdc` for full interface rules.

## Assertions

- Always use Playwright's auto-retrying `expect()` — never raw `.isVisible()` checks
- Prefer `toBeVisible()` over `toHaveCount(1)` for single elements
- Prefer `toContainText()` over `toHaveText()` when only part of the text matters
- Never assert implementation details (CSS classes, internal state) — assert what the user sees
