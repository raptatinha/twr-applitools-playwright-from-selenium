# Pre-PR Review Dimensions

Use this reference while executing `pre-pr-review`.

## 1) Leftover Artifacts

### Must scan in changed scope

- `TODO`, `FIXME`, `HACK`, `XXX`
- `console.log`, `console.debug`, `debugger`
- `it.only`, `describe.only`, `test.only`
- broad skip usage (`it.skip`, `describe.skip`, metadata skips)
- commented-out logic blocks
- placeholder constants, temporary values, and local test URLs

### Triage guidance

- Focus on staged, unstaged, and untracked files in the working tree.
- Ignore historical artifacts outside current changed scope.
- Escalate when artifact impacts runtime behavior, observability quality, or test reliability.

### Console log intentionality check

For each added `console.*` or `debugger`, classify:

- **Intentional diagnostics** if it is:
  - behind env/debug guards, or
  - in test/CI-only paths, or
  - clearly justified inline.
- **Unjustified debug artifact** if it is:
  - always-on in normal runtime with no rationale.

Report explicitly:

- `Intentional diagnostics` -> Suggestion or Praise
- `Unjustified debug artifact` -> Important (Critical if it exposes sensitive data)

## 2) Test Freshness

### Coverage alignment checks

- Behavior changes have corresponding test updates.
- New branches/filters/path guards include branch-focused assertions.
- Error paths and edge cases are represented.
- Renamed fields/routes/contracts are reflected in tests and fixtures.

### Anti-patterns

- Assertion removal without replacement.
- Tests weakened to avoid failures (over-mocking, broad skips).
- Snapshot churn without explanation.

### Severity guidance

- **Important**: behavior changed with little/no relevant test movement.
- **Critical**: high-risk auth/runtime logic changed with no effective tests.

## 3) Dead or Unused Code

### Static hygiene

- Added imports that are never used.
- Symbols removed in one area but still referenced elsewhere.
- Feature/config leftovers after refactors.
- Deprecated paths kept without justification.

### Refactor checks

- Renamed symbols are propagated consistently.
- Temporary compatibility shims are justified and documented.
- Docs/config/scripts match code-level changes where applicable.

### Severity guidance

- **Critical** only when dead paths can break runtime behavior.
- Otherwise mark **Important** for maintainability debt before PR.

## 4) Consistency Checks

### Codebase alignment

- Naming conventions match adjacent modules.
- Error handling follows local project patterns.
- Validation and API response styles remain consistent.
- Existing utilities are reused instead of introducing near-duplicates.

### Runtime and operational consistency

- Logging format/verbosity remains coherent.
- Defaults/fallback behavior stays predictable.
- Permission/auth checks are consistent across similar routes.

## 5) Interface Conventions

### Naming

- All interface names must start with the `I` prefix (e.g., `ICertificateEditFormProps`).
- Flag any interface missing the prefix as **Important**.

### Placement

- Interfaces must live in dedicated `*-types.ts` files.
- Flag interfaces defined inside `.tsx` files as **Important**.
- Flag interfaces defined inside test files (`.spec.ts`, `.page.ts`, `.fixtures.ts`, `.setup.ts`, `.api.ts`) as **Important**.
- Interfaces in service/utility `.ts` files are acceptable only when small, tightly coupled to one function, and not reused elsewhere — otherwise flag as **Suggestion**.

### Deduplication

- Scan for interfaces with overlapping field shapes across the changed scope.
- Flag near-duplicate interfaces as **Important** with a recommendation to extract a shared base.

### Severity guidance

- **Important**: missing `I` prefix, interface in `.tsx`/test file, or duplicate interface.
- **Suggestion**: interface in a utility `.ts` that could be extracted to a `*-types.ts` file.

## Cross-Dimension Extras

Apply when relevant:

- **Security posture**: no sensitive values added to logs, config, or frontend output.
- **Blast radius clarity**: key affected areas are identified for focused manual verification.
- **Operational readiness**: fast-fail and observability patterns are preserved.
- **Documentation sync**: developer docs are updated when behavior or conventions changed.

## 6) Production SQL Readiness

### Trigger

This dimension applies only when `prisma/schema.prisma` is in the changed scope. If it is not, skip entirely.

### Existence check

- If the schema adds or modifies tables, columns, enums, or defaults, a corresponding production SQL script must exist.
- Expected locations: `temp/*.sql`, `scripts/prod-database-release.sql`, or a new file in `prisma/migrations-prod/`.
- If no prod SQL script is found for a schema change, flag as **Critical** ("Production SQL missing for schema change").

### Consistency check

For every schema change, compare the Prisma schema with the SQL script:

- **Column types and widths** must match (e.g., `@db.VarChar(16)` → `VARCHAR(16)` in SQL).
- **Default values** must match (e.g., `@default("pt-BR")` → `DEFAULT 'pt-BR'`).
- **Enum values** must include all members defined in the Prisma schema.
- **New columns** present in schema must appear in the SQL script.
- **Nullable/NOT NULL** must agree between schema and SQL.

Flag any mismatch as **Critical**.

### Idempotency check

- Production SQL scripts must be safe to re-run (e.g., `IF NOT EXISTS` guards, `DROP PROCEDURE IF EXISTS` wrappers).
- Flag non-idempotent scripts as **Important**.

### Severity guidance

- **Critical**: missing prod SQL for schema change, or drift between schema and SQL (wrong types, defaults, missing columns/enums).
- **Important**: prod SQL exists but is not idempotent.
- **Suggestion**: prod SQL could use better comments or phase headers (before/after deploy).

## 7) Convention Rules Compliance

### Purpose

Ensures every changed file is checked against all applicable `.cursor/rules/*.mdc` convention files. Rules must not be silently skipped.

### Rule-to-glob mapping

| Rule file | Applies to (globs) |
|-----------|---------------------|
| `interface-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx` |
| `enum-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx` |
| `lint-and-ts-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx`, `platform/**/*.mts` |
| `e2e-conventions.mdc` | `tests/e2e/**/*.ts`, `tests/setup/**/*.ts`, `tests/api/**/*.ts`, `tests/unit/**/*.ts`, `tests/accessibility/**/*.ts` |
| `bulk-notification-conventions.mdc` | `platform/src/app/api/v1/**/bulk-*/**`, `platform/src/lib/*-notify*.ts`, `platform/src/lib/notification-cooldown.ts` |
| `ui-accessibility.mdc` | `platform/src/app/**/*.tsx`, `platform/src/components/**/*.tsx` |

### Workflow

1. For each changed file, match its path against all rule globs.
2. Read every matching `.mdc` file.
3. Audit the changed hunks against each rule's requirements.
4. Report violations grouped by rule file.
5. If no rules match a changed file, note it as "No conventions apply" (not a finding).

### Key checks per rule

- **interface-conventions**: `I` prefix, placement in `*-types.ts`, no duplicates.
- **enum-conventions**: `E` prefix, placement in `*-types.ts`, string enums, `z.enum()` not `z.nativeEnum()`.
- **lint-and-ts-conventions**: no `console.log`, no floating promises, no `@prisma/client` direct imports in `src/`, no `.only()`, complexity ≤ 10, max-lines ≤ 200.
- **e2e-conventions**: test pyramid (lowest viable level), semantic locators, no locators in spec files, POM pattern.
- **bulk-notification-conventions**: cooldown, tracking records, 429 handling.
- **ui-accessibility**: semantic HTML, proper labeling (label > aria-labelledby > aria-label), no ARIA roles on native elements.

### Severity guidance

- Use the severity defined in each rule file for its violations.
- When a rule file does not specify severity, default to **Important** for naming/placement violations and **Suggestion** for style preferences.

## 8) Route Visibility

### Trigger

This dimension applies whenever any `route.ts` file under `platform/src/app/api/` is added or modified.

### Visibility check

For each new route file:

1. Derive the URL path from the directory structure (e.g., `platform/src/app/api/v1/webhooks/stripe-test/route.ts` → `/api/v1/webhooks/stripe-test`).
2. Read `platform/src/proxy.ts` and collect all entries in `publicPaths`.
3. Determine visibility:
   - **Public** — the route path equals or starts-with a `publicPaths` entry.
   - **Private** — the path is not present; the global middleware will require a `twr_access_token` cookie.
4. Cross-check `platform/src/lib/routes.ts`: every public route should have a matching constant imported into `proxy.ts`. Flag gaps.

### Confirmation prompt

Always surface the finding as an explicit confirmation request. Tailor the message to whether the route is new or modified:

- **New route**: "Route `<path>` is **public / private**. Is this the intended visibility?"
- **Modified route**: "Route `<path>` is **public / private**. Has this change affected the intended visibility?"

Do not assume intent — ask the developer to confirm even when the classification appears correct.

### Severity guidance

- **Important**: new route added and visibility has been determined — developer confirmation required.
- **Critical**: route path is inconsistently wired (constant in `routes.ts` but absent from `publicPaths`, or vice versa).

## Finding Templates

Use concise, evidence-based language:

- **Problem**: What is wrong.
- **Impact**: Why it matters.
- **Recommendation**: Specific fix.

Example:

- Problem: auth guard behavior changed in route handlers, but tests were not updated.
- Impact: regressions in role-based access may go unnoticed until production.
- Recommendation: add API tests for allowed and denied role paths before opening PR.

author: Renata Andrade