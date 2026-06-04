---
description: "Use when creating, modifying, or organizing Playwright test files, page objects, API request files, or test data. Covers project structure, naming, POM, and data management."
applyTo: "playwright-automation-framework/**/*.ts"
author: Renata Andrade
---
# Playwright Architectural Guide

## Project Structure

```
tests/
├── api/
│   ├── requests/
│   │   └── <name>-request.ts
│   ├── specs/
│   │   └── <name>.spec.ts
│   └── README.md
├── commons/
│   ├── data/
│   │   └── <name>.ts
│   ├── interfaces/
│   │   └── <name>.ts
│   └── utils/
│       ├── api-endpoints.ts
│       ├── api-methods.ts
│       ├── api-paths.ts
│       ├── api-request-utils.ts
│       ├── api-url-builder.ts
│       ├── environment-base-url.ts
│       ├── pages.ts
│       ├── ui-pages.ts
│       ├── ui-url-builder.ts
│       └── <name>.ts
├── setup/
│   └── <name>.ts
└── ui/
    ├── pages/
    │   └── <name>-page.ts
    ├── specs/
    │   └── <name>.spec.ts
    └── README.md
.env.example
playwright.config.ts
```

## Naming Conventions

- **File names**: kebab-case (`foo-bar.ts`)
- **Classes, Interfaces, Types, Enums, Decorators**: `UpperCamelCase`
- **Variables, parameters, functions, methods, properties, module aliases**: `lowerCamelCase`
- **Global constants, enum values**: `CONSTANT_CASE`
- Names must be complete and meaningful

## Test File Organization

- Use the **AAA (Arrange, Act, Assert)** pattern
- Use **DDT (Data Driven Testing)** strategy for data management
- Tests must use **tags with the feature** at the beginning of the test name

## Page Object Model (POM)

- Define all locators (preferably in the constructor; filter in methods if needed)
- Prioritize `data-auto-qa` attributes (QEs should add them to the codebase when missing)
- If `data-auto-qa` unavailable, prioritize user-facing attributes
- Maximum **20 methods** per page object

## General Rules

- Files must not exceed **200 lines** (ideally < 100)
- All files must be inside meaningful folders — no loose files alongside folders
- **Sensitive data** must never be pushed to the repo — use `.env` files
- **Libraries** should not be added unless extremely necessary
- Use **API to create test data** as much as possible
- Evaluate whether tests should move to lower levels (API, Unit, Component)
- Each file has a single responsibility — data creation does NOT belong in test files
- Assertion messages must follow AAA pattern or be in a dedicated messages file

## Data Management

- Data should be managed at the **spec file level** (highest level)
- Request files must never handle data except to set defaults
- All created data must be **deleted after a test**
- Reusable data should be stored in the database (seed data via MR in core app repo)
- No **magic numbers** — use meaningful values
- No personal email or data — use generic information
- Use `_` (underscore) for data values, not `-` (dash)
  - Example: `invalidUsername: "invalid_user"`
- Prefix test data with `***PW` at the beginning

## Pipeline

- All tests must be enabled on the pipeline
- Flaky tests: investigate root cause as priority
  - If no fix found within 24 hours → skip or delete the test
- **Less than 100% pipeline success is not allowed**
