import { expect, type Locator, type Page } from "@playwright/test";

export class MessageContentPage {
  readonly page: Page;
  readonly messageContentTitle: Locator;
  readonly messageContentSMSEditButton: Locator;
  readonly messageContentEmailEditButton: Locator;
  readonly messageContentEditor: Locator;
  readonly messageContentSculptInput: Locator;
  readonly settingsButton: Locator;
  readonly saveDraftButton: Locator;
  readonly exitSculptEditorButton: Locator;
  readonly messageContentSculptButton: Locator;
  readonly messageContentSaveButton: Locator;
  readonly messageContentBackButton: Locator;
  readonly smsMessageContentEditor: Locator;
  readonly messageContentSuccessMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.messageContentEmailEditButton = page.getByTestId("html-editor-form-actions-button-edit");
    this.messageContentSMSEditButton = page
      .getByTestId("message-content-panel")
      .getByTestId("form-actions-button-edit");
    this.messageContentEditor = page.getByTestId("no-namespace-code-editor").getByRole("textbox");
    this.messageContentSculptInput = page.getByTestId("input-search");
    this.messageContentSculptButton = page.getByTestId("card-component");
    this.settingsButton = page.getByTestId("toolbar-button-Message_Settings");
    this.saveDraftButton = page.getByTestId("app-bar-button_Save_Draft save-draft");
    this.exitSculptEditorButton = page.getByTestId("app-bar-button_back-to-admin exit-sculpt");
    this.messageContentSaveButton = page.getByRole("button", { name: "Save" });
    this.messageContentBackButton = page.getByTestId("app-bar-button_back-to-admin");
    this.smsMessageContentEditor = page.getByRole("textbox");
    this.messageContentTitle = page.getByRole("heading", { name: "Message Content" });
    this.messageContentSuccessMessage = page.getByText("Message content has been updated successfully");
  }

  async openEmailMessageContent() {
    await this.messageContentEmailEditButton.click();
  }

  async fillMessageContentSculptNameAndClick(templateSculpt: string) {
    await this.messageContentSculptInput.fill(templateSculpt);
    await this.messageContentSculptButton.click();
  }

  async openSculptMessageEditorAndSaveDraft() {
    await this.settingsButton.click(); //strategy for issue caused by rendering all the templates
    await this.saveDraftButton.waitFor({ state: "visible" });
    await this.saveDraftButton.click();
    await this.exitSculptEditorButton.click({ force: true });
  }

  async openSMSMessageContent() {
    await this.messageContentSMSEditButton.click();
  }

  async fillBodyContent(messageContent: string) {
    await this.messageContentEditor.fill(messageContent);
  }

  async clickSaveMessageContent() {
    await this.messageContentSaveButton.click();
  }

  async clickBackMessageContent() {
    await this.messageContentBackButton.click();
  }

  async assertMessageContentTitleIsPresent() {
    await expect(this.messageContentTitle).toBeVisible();
  }

  async assertSuccessMessageContentUpdatedIsPresent() {
    await expect(this.messageContentSuccessMessage).toBeVisible();
    await this.messageContentSuccessMessage.click();
  }

  async fillSMSBodyContent(messageContent: string) {
    await this.smsMessageContentEditor.fill(messageContent);
  }
}
