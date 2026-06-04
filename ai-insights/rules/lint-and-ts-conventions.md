---
description: Lint rules, TypeScript conventions, and Prettier formatting for the platform app
globs: platform/**/*.ts, platform/**/*.tsx, platform/**/*.mts
author: Renata Andrade
---

# Lint and TypeScript Conventions

## ESLint Rules

The platform uses ESLint v9 flat config (`platform/eslint.config.mjs`) with `eslint-config-next` (core-web-vitals + typescript) as the base.

### Code Quality (all files)

- **complexity**: max 10 — break down complex functions
- **eqeqeq**: always use `===` and `!==`
- **max-lines**: max 200 lines per file — split large files
- **max-params**: max 5 parameters — use an options object for more
- **no-console**: only `console.warn()` and `console.error()` allowed; no `console.log()` in production code
- **no-fallthrough**: switch cases must break (empty cases allowed)
- **no-restricted-syntax**: `.only()` on tests is forbidden — prevents accidentally committing focused tests

### Type-Checked Rules (src/ and tests/)

These require type information and catch async bugs:

- **no-floating-promises**: every Promise must be `await`ed, returned, or explicitly voided
- **no-misused-promises**: async functions must not be passed where sync callbacks are expected (e.g. React event handlers)
- **no-explicit-any**: use proper types instead of `any`

### Import Sorting

`eslint-plugin-simple-import-sort` enforces consistent import order automatically. Let the auto-fixer handle sorting — do not manually rearrange imports.

### Prisma Import Restriction (src/ only)

Direct imports from `@prisma/client` are forbidden in `src/`. Always use the singleton:

```typescript
import { db } from "@/lib/db";
```

Never instantiate `PrismaClient` directly. The `db` proxy in `@/lib/db` handles connection pooling, hot-reload in dev, and stale-delegate detection.

### Vitest Rules (tests/unit/)

`eslint-plugin-vitest` recommended rules apply to `tests/unit/**/*.test.ts`. Follow Vitest best practices for `vi.mock`, assertions, and test structure.

## TypeScript Path Aliases

Defined in `platform/tsconfig.json`:

| Alias | Target | Usage |
|-------|--------|-------|
| `@/*` | `./src/*` | All application source code |
| `@requests/*` | `./tests/api/requests/*` | API request helpers for tests |
| `@setup/*` | `./tests/setup/*` | Test setup files (auth, fixtures) |

Always use these aliases instead of relative paths when importing across directory boundaries.

## Interfaces

See `interface-conventions.mdc` for full rules. Key points:

- All interfaces must use the `I` prefix (e.g., `ICertificateEditFormProps`)
- Interfaces must live in dedicated `*-types.ts` files — never in `.tsx` or test files
- No duplicate interfaces — search and reuse/extend existing ones
- Use `import type` for interface-only imports

## Prettier

Prettier is configured as the code formatter. Run `npm run format` to format all files.

- `eslint-config-prettier` is included in the ESLint config to disable rules that conflict with Prettier
- Do not add ESLint rules that enforce code style (spacing, semicolons, quotes) — Prettier handles formatting

## Scripts

| Script | Command | Purpose |
|--------|---------|---------|
| `npm run lint` | `eslint` | Run all lint rules |
| `npm run format` | `prettier --write .` | Auto-format all files |
| `npm run typecheck` | `tsc --noEmit` | Type-check without emitting |
