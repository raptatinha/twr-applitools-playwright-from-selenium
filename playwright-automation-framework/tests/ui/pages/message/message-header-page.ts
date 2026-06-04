import { expect, type Locator, type Page } from "@playwright/test";

export class MessageHeaderPage {
  readonly page: Page;
  readonly messageHeaderTitle: Locator;
  readonly messageHeaderEditButton: Locator;
  readonly messageHeaderSubjectInput: Locator;
  readonly messageHeaderFromEmailInput: Locator;
  readonly messageHeaderReplyEmailInput: Locator;
  readonly messageHeaderFromDescriptionInput: Locator;
  readonly messageHeaderSaveButton: Locator;
  readonly messageHeaderCancelButton: Locator;
  readonly messageHeaderSuccessMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.messageHeaderTitle = page.getByRole("heading", { name: "Message Header" });
    this.messageHeaderEditButton = page.getByTestId("message-header-panel").getByTestId("form-actions-button-edit");
    this.messageHeaderSuccessMessage = page.getByText("Message Header has been updated successfully");
    this.messageHeaderSubjectInput = page.locator('input[name="subject"]');
    this.messageHeaderFromEmailInput = page.locator('input[name="fromEmail"]');
    this.messageHeaderReplyEmailInput = page.locator('input[name="replyEmail"]');
    this.messageHeaderFromDescriptionInput = page.locator('input[name="fromDesc"]');
    this.messageHeaderSaveButton = page.getByTestId("form-actions-button-continue");
    this.messageHeaderCancelButton = page.getByTestId("form-actions-button-cancel");
  }

  async openMessageHeader() {
    await this.messageHeaderEditButton.click();
  }

  async fillSubject(subject: string) {
    await this.messageHeaderSubjectInput.fill(subject);
  }

  async fillFromEmail(fromEmail: string) {
    await this.messageHeaderFromEmailInput.fill(fromEmail);
  }

  async fillReplyEmail(replyEmail: string) {
    await this.messageHeaderReplyEmailInput.fill(replyEmail);
  }

  async fillFromDescription(fromDescription: string) {
    await this.messageHeaderFromDescriptionInput.fill(fromDescription);
  }

  async clickSaveMessageHeader() {
    await this.messageHeaderSaveButton.click();
  }

  async clickCancelMessageHeader() {
    await this.messageHeaderCancelButton.click();
  }

  async assertMessageHeaderTitleIsPresent() {
    await expect(this.messageHeaderTitle).toBeVisible();
  }

  async assertSuccessMessageHeaderUpdatedIsPresent() {
    await expect(this.messageHeaderSuccessMessage).toBeVisible();
    await this.messageHeaderSuccessMessage.click();
  }

  async assertSuccessMessageHeaderUpdatedIsNotPresent() {
    await expect(this.messageHeaderSuccessMessage).not.toBeVisible();
    await this.messageHeaderSuccessMessage.click();
  }
}
