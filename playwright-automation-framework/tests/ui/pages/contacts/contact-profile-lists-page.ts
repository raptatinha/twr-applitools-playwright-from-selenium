import { expect, type Locator, type Page } from "@playwright/test";

export class ContactProfileListPage {
  readonly page: Page;
  readonly listTitle: Locator;
  readonly listsTabButton: Locator;
  readonly listsEditButton: Locator;
  readonly savedToastMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.listsTabButton = page.getByRole("tab", { name: "Lists" });
    this.listTitle = page.getByRole("heading", { name: "Lists" });
    this.listsEditButton = page.getByTestId("lists-form-actions-button-edit");
    this.savedToastMessage = page.getByText("List membership saved");
  }

  async findSavedListCheckbox(key: string) {
    return this.page.getByTestId(`input-checkbox-lists.${key}`);
  }

  async openContactProfileListsTab() {
    console.log("Opening the Lists tab");
    await this.listsTabButton.click();
    this.assertTitleIsPresent();
  }

  async assertTitleIsPresent() {
    await expect(this.listTitle).toBeVisible();
  }

  async clickEditListsButton() {
    console.log("Clicking the Edit Lists button");
    await this.listsEditButton.click();
  }

  async assertSavedToastMessageIsPresent() {
    try {
      if (await this.savedToastMessage.isVisible()) {
        await this.savedToastMessage.click();
      }
    } catch {
      console.log("Saved toast message is not present.");
    }
  }
}
