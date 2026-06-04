---
name: pre-pr-review
description: Review working-tree changes before opening a PR by checking all uncommitted changes (staged, unstaged, and untracked) for leftover artifacts, test freshness, dead code, and consistency risks.
author: Renata Andrade
---

# Pre-PR Change Review

Complements repository code-review skills by focusing on merge readiness before a PR exists.

## Scope

Always review these dimensions:

1. Build Readiness
2. Leftover Artifacts
3. Test Freshness
4. Dead or Unused Code
5. Consistency Checks
6. Interface Conventions
7. Production SQL Readiness
8. Convention Rules Compliance
9. Route Visibility

This skill intentionally does not evaluate PR description quality or commit hygiene because no PR/commits are required yet.

## Input Mode

Accept:

- No argument (default mode)

This skill always reviews the current working tree.

## Source Resolution

1. Capture working-tree context from git:
   - `git status --short`
   - `git diff --name-only HEAD`
2. Resolve full review scope as:
   - staged changes (`git diff --staged`)
   - unstaged changes (`git diff`)
   - untracked files (`git ls-files --others --exclude-standard`)
3. Record: total changed files, staged/unstaged split, untracked count, and dominant directories.

## Diff Strategy

Primary analysis scope is all uncommitted changes:

- `git diff --staged` for staged hunks
- `git diff` for unstaged hunks
- full file reads for untracked files

When needed, use `git diff HEAD` as a consolidated view.

For large change sets, partition by subsystem/directory and review in this order:

1. API routes and auth/security-sensitive code
2. Shared runtime/library code
3. UI components and pages
4. Test files and test helpers

## Review Workflow

1. Gather metadata
   - file counts, directories, staged/unstaged/untracked split.
2. Load baseline criteria
   - read local guardrails and standards when available.
3. Run all seven review dimensions
   - use `references/review-dimensions.md` as the checklist.
4. Build findings
   - findings first, grouped by severity.
   - include file/symbol references and concrete remediation.
5. Produce verdict
   - `Ready to PR` or `Needs Cleanup`.

## Severity Rules

- **Critical**: correctness, security, data loss, or high-risk behavior change without meaningful tests.
- **Important**: high-confidence maintainability/regression risk that should be cleaned before opening PR.
- **Suggestion**: non-blocking improvements that strengthen quality.
- **Praise**: notable good practices worth keeping.

## Required Checks

### Build Readiness

Run the production build check to catch type errors before deploying:

```bash
cd platform && npm run build:check
```

This runs:
1. `prisma generate` — ensures Prisma client is up to date
2. `tsc --noEmit` — type-checks source files (same scope as `next build`)
3. `tsc --noEmit -p tsconfig.tests.json` — type-checks test files separately

If either type-check fails, flag as **Critical** with the error output. The deploy will fail with the same errors.

Then run ESLint on all changed and untracked platform files:

```bash
cd platform && npx eslint <changed-files>
```

Where `<changed-files>` is the list of `.ts`, `.tsx`, and `.mts` files from `git diff --name-only HEAD` and `git ls-files --others --exclude-standard`, filtered to `platform/` paths.

Key rules enforced by ESLint that `tsc` does not catch:
- `max-lines` (200 lines per file)
- `complexity` (max 10)
- `eqeqeq` (strict equality)
- `no-console` (only `warn`/`error` allowed)
- `no-floating-promises`, `no-misused-promises`

If ESLint reports errors, flag as **Critical** (same as type errors — these block merge).

### Leftover Artifacts

Scan changed hunks/new files for:

- `TODO`, `FIXME`, `HACK`, `XXX`
- commented-out code blocks
- `console.log`, `console.debug`, `debugger` (run intent check)
- `.only` / accidental `.skip`
- temporary placeholders, hardcoded secrets or private URLs

For added logs/debug code, classify explicitly:

- **Intentional diagnostics**:
  - behind env/debug gates, or
  - test-only diagnostics, or
  - justified with inline rationale.
- **Unjustified debug artifact**:
  - always-on runtime logs with no guard/rationale.

### Test Freshness

- Behavior changes should have corresponding test updates.
- New branches/error paths should be covered.
- Avoid assertion removals without replacement.
- Flag high-risk runtime changes with no test movement.

### Dead or Unused Code

- Unused imports/exports/symbols introduced by the changes.
- Obsolete references after refactors or renames.
- Stale flags/config/docs left after behavior changes.

### Consistency Checks

- Naming and structure align with nearby modules.
- Error handling follows repository patterns.
- New abstractions reuse existing utilities when possible.
- Auth, validation, and API contracts remain consistent.

### Interface Conventions

- All interfaces use the `I` prefix (e.g., `ICertificateEditFormProps`).
- Interfaces are in dedicated `*-types.ts` files, not in `.tsx` or test files.
- No duplicate or near-duplicate interfaces across the changed scope.
- See `.cursor/rules/interface-conventions.mdc` for full rules.

### Production SQL Readiness

When `prisma/schema.prisma` is in the changed scope:

- A corresponding production SQL script must exist (e.g., `temp/*.sql` or `scripts/prod-database-release.sql`).
- The SQL must be idempotent (IF NOT EXISTS guards, safe to re-run).
- Column types, widths, defaults, and enum values in the SQL must match the Prisma schema exactly.
- If the schema adds/modifies tables, columns, enums, or defaults and no prod SQL is found, flag as **Critical**.
- If prod SQL exists but drifts from the Prisma schema (different types, defaults, missing columns), flag as **Critical**.

When `prisma/schema.prisma` is NOT in the changed scope, skip this dimension.

### Route Visibility

When any `route.ts` file under `platform/src/app/api/` is added or modified:

1. Identify the URL path the route registers (derived from its directory path).
2. Check `platform/src/proxy.ts` `publicPaths` array to determine whether the route is public or auth-guarded.
   - A route is **public** if its path (or a parent) appears in `publicPaths`.
   - A route is **private** if it is not present in `publicPaths` (the global middleware will require authentication).
3. For every affected route, report the current visibility and ask the developer to confirm it is still correct.
   - For **new** routes: "Route `<path>` is **public/private**. Is this the intended visibility?"
   - For **modified** routes: "Route `<path>` is **public/private**. Has this change affected the intended visibility?"
4. Severity:
   - **Important**: route added or modified — developer must explicitly confirm visibility intent.
   - **Critical**: route path is inconsistently registered (e.g., constant added to `routes.ts` but missing from `publicPaths`, or added to `publicPaths` with no matching constant).

### Convention Rules Compliance

For every changed file, identify which `.cursor/rules/*.mdc` files apply based on their `globs` patterns:

| Rule file | Globs |
|-----------|-------|
| `interface-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx` |
| `enum-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx` |
| `lint-and-ts-conventions.mdc` | `platform/**/*.ts`, `platform/**/*.tsx`, `platform/**/*.mts` |
| `e2e-conventions.mdc` | `tests/e2e/**/*.ts`, `tests/setup/**/*.ts`, `tests/api/**/*.ts`, `tests/unit/**/*.ts`, `tests/accessibility/**/*.ts` |
| `bulk-notification-conventions.mdc` | `platform/src/app/api/v1/**/bulk-*/**`, `platform/src/app/api/v1/**/notify-*/**`, `platform/src/lib/*-notify*.ts`, `platform/src/lib/notification-cooldown.ts` |
| `ui-accessibility.mdc` | `platform/src/app/**/*.tsx`, `platform/src/components/**/*.tsx` |

For each changed file:

1. Match the file path against all rule globs above.
2. Read and apply every matching rule file.
3. Flag violations with the severity defined in the rule file.
4. If no rules match a changed file, note it as "No conventions apply".

This dimension ensures rules are not silently skipped when files are changed.

## Large Batch Behavior

This repository allows one PR containing a large feature pack. Do not recommend splitting solely due to size.

For large batches:

1. Partition review by subsystem so findings stay actionable.
2. Prioritize risk-heavy directories first.
3. Call out concentrated risk areas and targeted cleanup before PR creation.

## Output Format

Use this structure:

```markdown
# Pre-PR Review: <summary>

## Findings
- Critical: ...
- Important: ...
- Suggestion: ...
- Praise: ...

## Open Questions / Assumptions
- ...

## Build Readiness
| Check | Result | Errors | Severity |
|-------|--------|--------|----------|

## Leftover Artifacts
| File | Finding | Intentional? | Severity | Recommendation |
|------|---------|-------------|----------|----------------|

## Test Freshness
| Area | Observation | Risk | Recommendation |
|------|-------------|------|----------------|

## Dead or Unused Code
| File/Symbol | Observation | Recommendation |
|-------------|-------------|----------------|

## Consistency Checks
- ...

## Interface Conventions
| File | Finding | Severity | Recommendation |
|------|---------|----------|----------------|

## Production SQL Readiness
| Schema Change | Prod SQL Found? | Match? | Severity | Recommendation |
|---------------|-----------------|--------|----------|----------------|

## Convention Rules Compliance
| File | Matching Rules | Violations | Severity | Recommendation |
|------|---------------|------------|----------|----------------|

## Route Visibility
| Route File | URL Path | Visibility | Confirmed? | Severity | Notes |
|------------|----------|------------|------------|----------|-------|

## Verdict
Ready to PR | Needs Cleanup
```

## Report File Requirement (Mandatory)

Every run of this skill must create a Markdown report file on disk.

1. Create directory if missing: `temp/pre-pr-reviews/`
2. Write the full review report (using the output format above) to:
   - `temp/pre-pr-reviews/pre-pr-review-YYYYMMDD-HHMMSS.md`
3. Never skip file generation, even for quick or partial reviews.
4. In the final response, include:
   - verdict (`Ready to PR` or `Needs Cleanup`)
   - the saved report path
   - a short findings summary

## Validation Run

When asked to validate this skill:

1. Collect current working-tree scope.
2. Run all nine dimensions.
3. Produce a complete report using the output format and save it as a `.md` file.
4. Include at least:
   - one build readiness check result
   - one artifact scan judgment
   - one test freshness judgment
   - one dead-code judgment
   - one consistency judgment
   - one production SQL readiness judgment (when schema.prisma is changed)
   - one convention rules compliance judgment per changed file
   - one interface conventions judgment
   - one route visibility judgment per new `route.ts` file (when applicable)
