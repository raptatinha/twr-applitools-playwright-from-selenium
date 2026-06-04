import { expect, type Locator, type Page } from "@playwright/test";
import times from "@utils/times";

export class DashboardPage {
  readonly page: Page;
  readonly title: Locator;
  readonly dashboardSection: Locator;

  constructor(page: Page) {
    this.page = page;
    this.title = page.getByRole("heading", { name: "Dashboard" });
    this.dashboardSection = page.getByTestId("panel-menu-item-Dashboard");
  }

  async openDashboard() {
    await this.dashboardSection.click();
    this.assertDashboardTitleIsPresent();
  }

  async assertDashboardTitleIsPresent() {
    await expect(this.title).toBeVisible({ timeout: times.fortySeconds });
  }
}
