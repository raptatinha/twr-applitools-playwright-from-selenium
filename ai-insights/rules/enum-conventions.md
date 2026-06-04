---
description: Enum naming, placement, and usage rules for the platform app
globs: platform/**/*.ts, platform/**/*.tsx
author: Renata Andrade
---

# Enum Conventions

## Naming

All enum names MUST start with the `E` prefix:

```typescript
// GOOD
export enum EUserStatus { ... }
export enum EUserFilterStatus { ... }
export enum EWorkshopStatus { ... }

// BAD — missing E prefix
export enum UserStatus { ... }
export enum FilterStatus { ... }
```

## File Placement

Enums MUST live in their own dedicated `.ts` files — never inside `.tsx` component files, spec files, or test helpers.

### Where to put enum files

| Scope | Location | Example |
|-------|----------|---------|
| Shared across the app | `platform/src/types/<domain>.ts` | `platform/src/types/user.ts` |
| Scoped to a feature/route | `<feature-dir>/<feature>-types.ts` | `admin/certificates/certificate-types.ts` |
| Scoped to a lib module | Next to the module in a `-types.ts` file | `src/lib/email/mailer-types.ts` |

### Rules

1. **No enums in `.tsx` files** — extract to a sibling `*-types.ts` file and import.
2. **No enums in test files** — spec files (`*.spec.ts`), page object models (`*.page.ts`), fixtures, setup files, and API request helpers must not define enums. Test-only enums go in `tests/commons/enums/`.
3. **Prefer string enums** over numeric enums so values are readable in API responses, CSV exports, and logs.

## Zod Integration

Use `z.enum(EMyEnum)` — do **not** use the deprecated `z.nativeEnum`:

```typescript
// GOOD
status: z.enum(EUserFilterStatus).optional()

// BAD — deprecated
status: z.nativeEnum(EUserFilterStatus).optional()
```

## Importing

Use `import type` for enum-only imports to enable proper tree-shaking:

```typescript
import type { EUserStatus } from "@/types/user";
```

> Exception: enums used as values (e.g., in `if (status === EUserFilterStatus.Active)`) require a regular import, not `import type`.

## No Duplicates

Each enum MUST be defined exactly once. Before creating a new enum, search `platform/src/types/` and nearby `*-types.ts` files for an existing enum that covers the same domain.

## Quick Reference

- Prefix: `E` (e.g., `EUserStatus`, `EWorkshopStatus`)
- Location: dedicated `*-types.ts` or `platform/src/types/<domain>.ts` files only
- No enums in: `.tsx`, `.spec.ts`, `.page.ts`, `.fixtures.ts`, `.setup.ts`, `.api.ts`
- Prefer string enums
- Zod: `z.enum(EMyEnum)` — not `z.nativeEnum`
- No duplicates: search before creating
