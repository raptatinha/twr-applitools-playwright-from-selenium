# 🎭 Applitools Test Automation Repository

> From Selenium to Playwright — a real-world migration showcase.

This repository contains the test automation frameworks and supporting documentation for the Applitools project.

---

## 📂 Content

### `ai-insights/`

Instruction and rule files that guide AI coding assistants (GitHub Copilot, Cursor, etc.) to follow the team's conventions:

| Subfolder | Purpose |
|-----------|---------|
| `copilot/` | Branching & MR workflows, code review standards, Playwright architecture |
| `rules/` | Coding convention rules for e2e tests, enums, interfaces, lint/TS |
| `skills/` | AI skill definitions for debugging and pre-PR reviews |

### `docs/`

Project documentation, including an explanation of how test fixtures are structured and used across the framework.

### `playwright-automation-framework/`

The primary E2E and API test automation framework built with **TypeScript** and **Playwright**. Contains UI tests, API tests, shared utilities, page objects, and configuration files.

### `selenium-automation-framework/`

The legacy test automation framework built with **Java** and **Selenium/TestNG**. Retained for reference and for tests not yet migrated to Playwright.

---

## 🗂️ Folder Structure

```
├── ai-insights/
│   ├── copilot/
│   ├── rules/
│   └── skills/
├── docs/
├── playwright-automation-framework/
└── selenium-automation-framework/
```

---

## 📚 Useful Resources

See [docs/resources.md](docs/resources.md).

---

<p align="center">
  <a href="https://testingwithrenata.com/">testingwithrenata.com</a><br/>
  Happy Testing 🎭
</p>