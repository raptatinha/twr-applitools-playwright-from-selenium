import { expect, type Locator, type Page } from "@playwright/test";
import times from "@utils/times";
import { EChannelKey } from "@enums/contact-enum";
import { EClassification, EEditor } from "@enums/message-enum";
import { ENavigationMenu } from "@enums/navigation-menu-enum";
import { IMessage } from "@interfaces/message-interface";
import { NavigationMenuPage } from "@pages/general/navigation-menu-page";

export class MessageCreateNewPage {
  readonly page: Page;
  readonly modalTitle: Locator;
  readonly messageNameInput: Locator;
  readonly htmlEditorCheckbox: Locator;
  readonly promotionalCheckbox: Locator;
  readonly sculptEditorCheckbox: Locator;
  readonly transactionalCheckbox: Locator;
  readonly submitButton: Locator;
  readonly cancelButton: Locator;
  readonly successMessagePopup: Locator;
  readonly channelDropdown: Locator;
  readonly channelDropdownValue: Locator;
  readonly loadingIndicatorDots: Locator;

  constructor(page: Page) {
    this.page = page;
    this.modalTitle = page.getByTestId("modal-heading");
    this.messageNameInput = page.getByTestId("field-name");
    this.channelDropdown = page.locator("//div[@data-id='wrapper-field-channel']//input[@class='Select__input']");
    this.channelDropdownValue = page.locator("//input[@name='channel']");
    this.htmlEditorCheckbox = page.getByLabel("HTML Editor");
    this.promotionalCheckbox = page.getByLabel("Promotional");
    this.sculptEditorCheckbox = page.getByLabel("Sculpt Editor");
    this.transactionalCheckbox = page.getByLabel("Transactional");
    this.submitButton = page.getByTestId("form-actions-button-continue");
    this.cancelButton = page.getByTestId("form-actions-button-cancel");
    this.successMessagePopup = page.getByText("New Message has been created successfully.");
    this.loadingIndicatorDots = page.locator("//div[contains(@class,'loadingIndicator')]");
  }

  private async getChannelDropdownOption(channelKey: string) {
    return this.page.locator(`//li[@data-id='field-channel--select-option--${channelKey}']`);
  }

  async openCreateMessageModal(menu: ENavigationMenu = ENavigationMenu.MESSAGES_CREATE_NEW_MESSAGE) {
    await new NavigationMenuPage(this.page).goToPage(menu);
  }

  async fillMessageName(messageName: string) {
    await expect(this.loadingIndicatorDots)
      .toBeHidden()
      .catch(async () => {
        // Do nothing, the expect is to avoid a failure due to the loading indicator
      });
    await this.messageNameInput.fill(messageName);
  }

  async clickContinue() {
    await this.submitButton.click();
  }

  async clickCancel() {
    await this.cancelButton.click();
  }

  async assertModalTitleIsPresent() {
    await expect(this.modalTitle).toBeVisible();
  }

  async assertSuccessMessagePopupIsPresent() {
    await expect(this.successMessagePopup).toBeVisible();
    await this.successMessagePopup.click();
  }

  async selectEditor(editor: string) {
    switch (editor) {
      case EEditor.HTML:
        await this.htmlEditorCheckbox.check();
        break;
      case EEditor.SCULPT:
        await this.sculptEditorCheckbox.check();
        break;
      default:
        console.error("Editor not implemented");
        break;
    }
  }

  async selectClassification(classification: string) {
    switch (classification) {
      case EClassification.PROMOTIONAL:
        await this.promotionalCheckbox.check();
        break;
      case EClassification.TRANSACTIONAL:
        await this.transactionalCheckbox.check();
        break;
      default:
        console.error("Classification not implemented");
        break;
    }
  }

  async selectChannelInDropdown(channelKey: string) {
    const channelDropdownOption = await this.getChannelDropdownOption(channelKey);
    await expect(async () => {
      await expect(this.loadingIndicatorDots)
        .toBeHidden()
        .catch(async () => {
          // Do nothing, the expect is to avoid a failure due to the loading indicator
        });
      await this.channelDropdown.click({ delay: times.oneSecond });
      await this.channelDropdown.pressSequentially(channelKey, { delay: times.hundredMillisecond });
      await channelDropdownOption.click();
    }).toPass({ intervals: [times.oneSecond], timeout: times.thirtySeconds });
    await expect(this.channelDropdownValue).toHaveAttribute("value", channelKey);
  }

  async selectChannelHubOptions(message: IMessage) {
    await this.selectChannelInDropdown(message.channelKey);
    switch (message.channelKey) {
      case EChannelKey.EMAIL:
        await this.selectEditor(message.editor);
        await this.selectClassification(message.classification);
        break;
      case EChannelKey.SMS:
        break;
      default:
        console.log("Channel not implemented");
        break;
    }
  }
}
