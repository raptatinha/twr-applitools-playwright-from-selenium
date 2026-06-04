import { expect, type Locator, type Page } from "@playwright/test";

export class MessageHubPage {
  readonly page: Page;
  readonly messageHubTitle: Locator;
  readonly sendMessageButton: Locator;
  readonly sendModalCancelButton: Locator;
  readonly sendModalConfirmButton: Locator;
  readonly messageSentSuccessMessage: Locator;
  readonly messageScheduleSentSuccessMessage: Locator;
  readonly sendTestButton: Locator;
  readonly publishButton: Locator;
  readonly draftPublishSuccessMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.messageHubTitle = page.getByTestId("left-pane-title");
    this.sendMessageButton = page.getByTestId("left-nav-button-send-message");
    this.sendModalCancelButton = page.getByTestId("alert-action-cancel");
    this.sendModalConfirmButton = page.getByTestId("alert-action-confirm");
    this.messageSentSuccessMessage = page.getByText("Message has been sent successfully");
    this.messageScheduleSentSuccessMessage = page.getByText("Message has been scheduled successfully");
    this.sendTestButton = page.getByRole("button", { name: "Send Test" });
    this.publishButton = page.getByRole("button").filter({ hasText: "Publish" });
    this.draftPublishSuccessMessage = page.getByText("Draft has been published successfully.");
  }

  async assertMessageHubTitleIsPresent() {
    await expect(this.messageHubTitle).toBeVisible();
  }

  async clickSendMessage() {
    await this.sendMessageButton.click();
  }

  async clickCancelSendModal() {
    await this.sendModalCancelButton.click();
  }

  async clickConfirmSendModal() {
    await this.sendModalConfirmButton.click();
  }

  async assertSuccessMessageSentPopupIsPresent() {
    await expect(this.messageSentSuccessMessage).toBeVisible();
    await this.messageSentSuccessMessage.click();
  }

  async assertSuccessScheduleMessagePopupIsPresent() {
    await expect(this.messageScheduleSentSuccessMessage).toBeVisible();
    await this.messageScheduleSentSuccessMessage.click();
  }

  async clickSendTest() {
    await this.sendTestButton.click();
  }

  async clickPublishDraft() {
    await this.publishButton.click();
  }

  async assertDraftPublishedSuccessfully() {
    await expect(this.draftPublishSuccessMessage).toBeVisible();
    await this.draftPublishSuccessMessage.click();
  }
}
