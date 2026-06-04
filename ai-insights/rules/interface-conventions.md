---
description: Interface naming, placement, and deduplication rules for the platform app
globs: platform/**/*.ts, platform/**/*.tsx
author: Renata Andrade
---

# Interface Conventions

## Naming

All interface names MUST start with the `I` prefix:

```typescript
// GOOD
export interface ICertificateEditFormProps { ... }
export interface IUserDetail { ... }
export interface ISendEmailOptions { ... }

// BAD — missing I prefix
export interface CertificateEditFormProps { ... }
export interface UserDetail { ... }
export interface SendEmailOptions { ... }
```

## File Placement

Interfaces MUST live in their own dedicated `.ts` files — never inside `.tsx` component files, spec files, page object models, or test helpers.

### Where to put interface files

| Scope | Location | Example |
|-------|----------|---------|
| Shared across the app | `platform/src/types/<domain>.ts` | `platform/src/types/api.ts` |
| Scoped to a feature/route | `<feature-dir>/<feature>-types.ts` | `admin/certificates/certificate-types.ts` |
| Scoped to a lib module | Next to the module in a `-types.ts` file | `src/lib/email/mailer-types.ts` |

### Rules

1. **No interfaces in `.tsx` files** — extract to a sibling `*-types.ts` file and import.
2. **No interfaces in test files** — spec files (`*.spec.ts`), page object models (`*.page.ts`), setup files (`*.setup.ts`), and API request helpers (`*.api.ts`) must not define interfaces. **Exception:** fixture type interfaces (e.g., `IAdminFixtures`, `ICommonFixtures`) are defined in `tests/e2e/fixtures/*.fixtures.ts` files since they are tightly coupled to the fixture implementation.
3. **No interfaces in service/utility `.ts` files** — extract to a sibling `*-types.ts` file. Exception: a small interface tightly coupled to a single function (e.g., function params) may stay in the same file only if it is not used anywhere else.

### Importing

Components and modules import interfaces from the `*-types.ts` file:

```typescript
import type { ICertificateEditFormProps } from "./certificate-types";
```

Use `import type` for interface-only imports to enable proper tree-shaking.

## No Duplicates

Each interface MUST be defined exactly once. Before creating a new interface:

1. **Search the codebase** for existing interfaces with the same or similar shape.
2. **Reuse or extend** an existing interface instead of creating a near-duplicate.
3. **Refactor duplicates** — if two interfaces share most fields, extract a common base and extend it.

```typescript
// GOOD — extend instead of duplicate
export interface IBaseUser {
  id: string;
  name: string;
  email: string;
}

export interface IUserDetail extends IBaseUser {
  role: string;
  workshopCount: number;
}

// BAD — duplicated fields across files
// file A: interface IUserRow { id: string; name: string; email: string; status: string; }
// file B: interface IUserDetail { id: string; name: string; email: string; role: string; }
```

### Duplicate check before creating

When asked to create or add an interface:

1. Search `platform/src/types/` and nearby `*-types.ts` files for overlapping shapes.
2. If a match exists, extend or reuse it.
3. If no match exists, create the new interface in the appropriate `*-types.ts` file with the `I` prefix.

## Quick Reference

- Prefix: `I` (e.g., `IProps`, `IUserDetail`)
- Location: dedicated `*-types.ts` files only
- No interfaces in: `.tsx`, `.spec.ts`, `.page.ts`, `.fixtures.ts`, `.setup.ts`, `.api.ts`
- No duplicates: search before creating, extend when possible
- Import style: `import type { IFoo } from "./foo-types"`
