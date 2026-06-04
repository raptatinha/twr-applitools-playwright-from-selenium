import { test as base } from "@playwright/test";
import { PageManager } from "@pages/page-manager";

// eslint-disable-next-line @typescript-eslint/consistent-type-definitions
export type TestOptions = {
  openPage: string;
  pm: PageManager;
};

export const test = base.extend<TestOptions>({
  openPage: async ({ page }, use) => {
    const URL = await page.goto("");
    console.log("========= Opening ", URL?.url());
    await use("");
  },

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  pm: async ({ page, openPage }, use) => {
    const pageManager = new PageManager(page);
    await use(pageManager);
  },
});

export { expect } from "@playwright/test";
