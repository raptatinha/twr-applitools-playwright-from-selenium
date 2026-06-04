## UI & API Tests for Acme

### 🧪 Framework

This project uses [Playwright](https://playwright.dev/) with [TypeScript](https://www.typescriptlang.org/).

### 🌇 Architecture

Check [Playwright Architectural Guide](../.github/instructions/playwright-architecture.instructions.md).

### 🎮 Install and Run the Project

#### 🧬 Dependencies

<!-- Add your dependencies here -->

#### 📥 Clone

<!-- Add your instructions here -->

#### 🪄 Install

<!-- Add your instructions here -->

#### 🗝️ Run

Check [package.json](package.json) for more scripts.

<!-- Add your instructions here -->

### 🪩 Code standards

To help enforce consistent coding practices we use a mix of

- [Husky](https://github.com/typicode/husky)
- [ESLint](https://eslint.org/)
- [Prettier](https://prettier.io/)

Husky is a tool that will install a pre-commit hook to run the linter any time before you attempt to make a commit.

Run:

- `npm run lint:fix` to fix lint issues.
- `npm run prettier:fix` to fix prettier issues.

If you ever run into a situation where you want to break the rules and force a commit, you can add a `--no-verify` option to the end of your git commit message. Ex: `git commit -m "forcing the commit" --no-verify`. This is not something that should be done often if even at all, but this will allow you to bypass the checks if needed. If you force the commit more than likely other folks that work in your repo will be faced with the same error when they pull down the changes that caused the error. USE WISELY....

More [here](https://playwrightsolutions.com/the-definitive-guide-to-api-test-automation-with-playwright-part-8-adding-eslint-prettier-and-husky/).
