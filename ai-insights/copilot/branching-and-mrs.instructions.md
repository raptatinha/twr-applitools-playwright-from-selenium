---
description: "Use when creating branches, writing commit messages, opening merge requests (MRs), or reviewing MR labels and workflows in GitLab."
applyTo: ""
author: Renata Andrade
---
# Branching Strategy, MRs & Code Review Standards

## 1. Branch Naming

### DO
- Use meaningful, descriptive names starting with a verb or noun
- Separate words with dashes (`-`)
- Examples: `upgrade-playwright-160`, `test-message-transactional-event-trigger`, `fix-pipeline-empty-var`

### DON'T
- Use the ticket number alone (e.g., `ACME-12314`)
- Use slashes (`/`) in the name
- Use camelCase or PascalCase (e.g., `PlaywrightUpgrade160`)
- Use generic names (e.g., `small-fixes`, `more-fixes`)

## 2. Commit Messages

Commit early, commit often. Messages **must** include a subject line and **may** include body text (separated by a blank line).

### Subject Line
- Under 50 characters
- Written in imperative mood ("Fix", not "Fixed")
- Capitalized
- No trailing period
- May include Jira ticket (e.g., `ACME-18162 Update readme`) — keep consistent across commits

### Body Text
- Wrap at 72 columns
- Focus on **what** and **why**, not how

### Standard Terminology

| Term | Meaning |
|------|---------|
| Add | Create a capability (feature, test) |
| Cut | Remove a capability |
| Fix | Resolve an issue (bug, typo) |
| Bump | Update a version |
| Make | Change tooling or build process |
| Start | Begin something (e.g., create feature flag) |
| Stop | End something (e.g., remove feature flag) |
| Refactor | Code-only changes |
| Reformat | Formatting only (whitespace) |
| Optimize | Improve performance |
| Document | Update documentation |

### Good Examples
- `ACME-18160 Refactor method waitForItemToBeFound for readability`
- `Remove unused method getLocators`
- `Upgrade playwright version 1.0.0`

### Bad Examples
- `Fixed bug with Y ACME-18380`
- `More fixes for broken stuff`
- `42`

## 3. Updating a Branch

- Update feature branches with `main` at least daily
- Use `git merge origin main`
- Resolve conflicts carefully and verify in GitLab after pushing

## 4. Opening an MR

Open MRs early, even if work isn't complete (use "Mark as draft").

### Title
- Follow commit message standards
- Include Jira ticket number(s) at the beginning
- Use "Draft:" prefix for incomplete work

### Description
- Use the "Default Template" and update all fields
- List all added/changed items with Jira ticket number(s)
- Check the Creator Checklist boxes only for items you actually performed

### Assignees & Reviewers
- Always assign yourself
- Assign 1–2 reviewers only when MR is ready (not in Draft mode)
- For early feedback on Draft MRs, use comments/tags instead

### Labels

| Label | When to Use |
|-------|-------------|
| `DO NOT MERGE` | Still making changes or there are blockers (add comment explaining why) |
| `IN REVIEW` | Reviewer is actively reviewing (added by reviewer) |
| `ON HOLD` | Paused effort (add comment explaining why) |
| `PENDING COMMENTS` | Review done, threads need addressing before merge |
| `REVIEWED` | Review complete, no open threads, but something else blocks merge |

### After Opening
- Verify changes in "Changes" tab
- Check Creator Checklist items
- Address failing pipeline tests
- Prompt team if no review after 1–2 days
- Respond to all Change Requests

## 5. Performing a Code Review

- Add `IN REVIEW` label when starting
- Look for: code inconsistencies, bad practices, confusing methods, naming standards
- Use "Add comment now" for timely feedback
- When done: add `PENDING COMMENTS` or approve and merge
- For second/third reviews, mark threads as completed when implementation meets expectations

## 6. Resolving Threads as Assignee

- Leave a comment ("Fixed", "Done") when pushing a change
- Keep conversations in GitLab (not private Slack)
- If a call is needed, add conclusions to the thread afterward
- Do NOT mark threads as resolved (reviewer's responsibility)
- Do NOT use "Auto-merge" (reviewer decides when to merge)
