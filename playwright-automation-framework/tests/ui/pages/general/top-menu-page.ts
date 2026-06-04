import { type Locator, type Page } from "@playwright/test";

export class TopMenuPage {
  readonly page: Page;
  readonly settingsButton: Locator;
  readonly logoutButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.settingsButton = page.getByTestId("button-component").last();
    this.logoutButton = page.getByText("Log Out");
  }

  private async clickSettingsButton() {
    await this.settingsButton.click();
  }

  async doLogout() {
    await this.clickSettingsButton();
    await this.logoutButton.click();
  }
}
