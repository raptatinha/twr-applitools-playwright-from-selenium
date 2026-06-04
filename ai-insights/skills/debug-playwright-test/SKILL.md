---
name: debug-playwright-test
description: Debug failed or flaky Playwright tests by rerunning with tracing and analyzing the trace via `npx playwright trace` CLI. Use when a Playwright test fails, flakes, or the user asks why a test is broken.
author: Renata Andrade
---

# Debug Failed Playwright Test

You are debugging a failed Playwright test. Follow this workflow in order.

## Rules

- Run commands from `platform/`.
- Use `npx playwright trace` (CLI mode), not `npx playwright show-trace` (GUI).
- If the rerun passes, report that the failure is not reproducible and stop.
- Prefer evidence from trace output over guessing.

## Step 0: Fast-path — read the HTML report data files

If the user points to a Playwright HTML report (e.g. `playwright-report/index.html`), **do not parse the HTML**. The `index.html` is a bundled SPA and is too large to read usefully.

Instead, look for the sibling `data/` folder — it contains pre-extracted `.md` files with structured failure details:

1. List the data folder:
   ```bash
   ls <report-dir>/data/*.md
   ```
2. Read **all `.md` files** in batch. Each file contains:
   - **Test info** — name, file, line
   - **Error details** — assertion message, expected vs received
   - **Page snapshot** — ARIA tree at the time of failure
   - **Test source** — the relevant code with the failing line marked

3. Group failures by root cause (same error, same component, same feature flag, etc.) and summarize before proceeding.

### Filtering expected failures

Before grouping, cross-reference with `test-results/.last-run.json` — its `failedTests` array contains only **actual** failures. Tests using Playwright's `test.fail(condition, reason)` annotation are **expected failures** when the condition is true: they appear in the HTML report data but are not real failures. Exclude them from the failure count and diagnosis. Mention them separately as "expected failures (environment/config)" so the user is aware, but do not propose code fixes for them.

This gives you everything needed to diagnose most failures without rerunning. If a failure needs deeper investigation (timing, network, flakiness), continue to Step 1 for that specific test.

## Step 1: Find the test

Search for the test title or keyword in spec files, then read the matching spec to understand the intent.

```bash
rg "<test title or keyword>" tests --glob "**/*.spec.ts"
```

## Step 2: Run the test with tracing

Run only the target test with tracing forced on and retries disabled:

```bash
npx playwright test -g "<exact test title>" --trace on --retries 0
```

If the test passes, inform the user and stop.

## Step 3: Read terminal output

If the test fails, extract:

- The assertion/error message
- The failing file and line
- The trace zip path (usually in `test-results/`)

If the trace path is not obvious, locate trace files:

```bash
rg --files test-results -g "*trace.zip"
```

## Step 4: Analyze the trace with CLI

Important: use `npx playwright trace` (CLI), not `npx playwright show-trace`.

### 4.1 Open the trace

```bash
npx playwright trace open <path-to-trace.zip>
```

### 4.2 List actions and find failures

```bash
npx playwright trace actions
```

Failed actions are marked with `x`.

### 4.3 Narrow to failed actions only

```bash
npx playwright trace actions --errors-only
```

### 4.4 Inspect the failing action

```bash
npx playwright trace action <action-id>
```

This shows action details, error output, expected vs received values, source location, and available snapshots.

### 4.5 Inspect page state around failure

```bash
npx playwright trace snapshot <action-id> --name before
npx playwright trace snapshot <action-id> --name input
npx playwright trace snapshot <action-id> --name after
```

The `after` snapshot is usually most useful for failed assertions.

### 4.6 Query snapshot DOM for deeper evidence

```bash
npx playwright trace snapshot <action-id> -- eval "document.title"
npx playwright trace snapshot <action-id> -- eval "document.querySelector('.error')?.textContent"
```

### 4.7 Check network requests (if relevant)

```bash
npx playwright trace requests
npx playwright trace requests --failed
npx playwright trace request <request-id>
```

Optional filters:

```bash
npx playwright trace requests --grep "api"
npx playwright trace requests --method POST
```

### 4.8 Check console and runtime errors

```bash
npx playwright trace console
npx playwright trace console --errors-only
npx playwright trace errors
```

Optional console views:

```bash
npx playwright trace console --browser
npx playwright trace console --stdio
```

### 4.9 Capture visual evidence or attachments

```bash
npx playwright trace screenshot <action-id>
npx playwright trace attachments
npx playwright trace attachment <attachment-id>
```

Optional attachment output path:

```bash
npx playwright trace attachment <attachment-id> -o out.png
```

### 4.10 Close trace session

```bash
npx playwright trace close
```

## Step 5: Report findings

Provide a concise summary:

- Which test failed (path + title)
- What went wrong (root cause)
- Evidence (specific trace action output and snapshot/request/console details)
- Failing line (exact file line from stack trace)
- Suggested fix (concrete change in test or app)

## Step 6: Apply the fix and verify

If root cause is clear and fix is straightforward, apply the fix and rerun:

```bash
npx playwright test -g "<exact test title>" --retries 0
```

If the issue appears to be an application bug, missing test data, or environment/config problem, explain that to the user and propose the next best step instead of guessing.

### Fix conventions

All fixes MUST follow the rules in `.cursor/rules/e2e-conventions.mdc`. Read that file before applying any change to test code. Key constraints:

- **Locator priority**: `getByRole()` → `getByLabel()` → `getByText()` → `getByPlaceholder()` → `getByTestId()` (last resort). Never use XPath or complex CSS selectors.
- **Fix the UI source when needed**: if the element has no suitable semantic locator, add `aria-label`, `role`, or `data-testid` to the component rather than resorting to fragile selectors.
- **No locators in spec files**: all element interaction goes through Page Object Models in `tests/e2e/pages/*.page.ts`.
- **POM locator placement**: reusable locators are `private readonly` fields assigned in the constructor. Only parameterized or one-off chained locators may stay inline in methods.
- **No `test.skip` or `waitForTimeout`**: tests must fail if prerequisites are missing; use auto-retrying `expect()` or `waitForLoadState()`.
- **No interfaces in test files**: if a type is needed, define it in a `*-types.ts` file in the source code.
- **Import routes from `src/lib/routes.ts`**: never hardcode URL paths.
- **No direct database access**: no Prisma, no raw SQL, no `mysql2` in tests.

## Trace Subcommands Reference (Playwright 1.59+)

All commands below are available under `npx playwright trace`. After `open`, commands operate on the currently opened trace.

| Subcommand | Example | Purpose | Useful options |
|---|---|---|---|
| `open` | `npx playwright trace open <trace.zip>` | Open and extract trace, print metadata | - |
| `close` | `npx playwright trace close` | Close and clean up extracted trace data | - |
| `actions` | `npx playwright trace actions` | List all actions with IDs and timing | `--grep`, `--errors-only` |
| `action` | `npx playwright trace action <id>` | Show full details for one action | - |
| `requests` | `npx playwright trace requests` | List network requests | `--grep`, `--method`, `--failed` |
| `request` | `npx playwright trace request <id>` | Show full details for one request | - |
| `console` | `npx playwright trace console` | Show console + stdio messages | `--errors-only`, `--browser`, `--stdio` |
| `errors` | `npx playwright trace errors` | Show errors with stack traces | - |
| `snapshot` | `npx playwright trace snapshot <id>` | View snapshot or run a browser command on snapshot | `--name`, `-- eval`, `-- screenshot`, `--filename` |
| `screenshot` | `npx playwright trace screenshot <id>` | Save screencast screenshot for an action | - |
| `attachments` | `npx playwright trace attachments` | List attachments in trace | - |
| `attachment` | `npx playwright trace attachment <id>` | Extract one attachment | `-o` |
| `help` | `npx playwright trace help [command]` | Show command help | - |

## Typical Investigation Sequence

```bash
npx playwright trace open test-results/<test-folder>/trace.zip
npx playwright trace actions --errors-only
npx playwright trace action <id>
npx playwright trace snapshot <id> --name after
npx playwright trace requests --failed
npx playwright trace console --errors-only
npx playwright trace errors
npx playwright trace close
```
