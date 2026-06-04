# Applitools Test Automation Repository

This repository contains the test automation frameworks and supporting documentation for the Applitools project.

## Content

### `ai-insights/`

Instruction and rule files that guide AI coding assistants (GitHub Copilot, Cursor, etc.) to follow the team's conventions:

- **`copilot/`** — High-level instruction files covering branching & MR workflows, code review standards, and Playwright architectural patterns.
- **`rules/`** — Granular coding convention rules for e2e tests, enums, interfaces, and lint/TypeScript settings.
- **`skills/`** — Reusable AI skill definitions for tasks like debugging Playwright tests and performing pre-PR reviews.

### `docs/`

Project documentation, including an explanation of how test fixtures are structured and used across the framework.

### `playwright-automation-framework/`

The primary E2E and API test automation framework built with TypeScript and Playwright. Contains UI tests, API tests, shared utilities, page objects, and configuration files.

### `selenium-automation-framework/`

The legacy test automation framework built with Java and Selenium/TestNG. Retained for reference and for tests not yet migrated to Playwright.

## Folder Structure

```
repo/
├── ai-insights/
│   ├── copilot/          # IDE instruction files for AI assistants
│   ├── rules/            # Coding convention rules (enums, interfaces, linting)
│   └── skills/           # Custom AI skills (debugging, pre-PR review)
├── docs/                 # Project documentation
├── playwright-automation-framework/   # Primary test framework (TypeScript + Playwright)
└── selenium-automation-framework/     # Legacy test framework (Java + Selenium)
```

https://testingwithrenata.com/

Happy Testing 🎭