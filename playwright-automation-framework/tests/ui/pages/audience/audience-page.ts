import { expect, type Locator, type Page } from "@playwright/test";
import { isSmsChannelKey } from "@utils/session-details";
import { ERulesOperator } from "@enums/audience-builder-enum";
import { EChannelKey } from "@enums/contact-enum";

export class AudiencePage {
  readonly page: Page;
  readonly audienceTitle: Locator;
  readonly audienceEditButton: Locator;
  readonly allContactsCheckbox: Locator;
  readonly saveButton: Locator;
  readonly emailAddressRule: Locator;
  readonly channelAddressInput: Locator;
  readonly includeField: Locator;
  readonly continueButton: Locator;
  readonly contactsCounterLabel: Locator;
  readonly activeANDForExclude: Locator;
  readonly activeORForExclude: Locator;
  readonly firstANDForInclude: Locator;
  readonly firstORForInclude: Locator;
  readonly messageAudienceSuccessMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.audienceTitle = page.getByRole("heading", { name: "Audience" });
    this.audienceEditButton = page.getByTestId("audience-form-actions-button-edit");
    this.allContactsCheckbox = page.getByLabel("All Contacts");
    this.saveButton = page.getByRole("button", { name: "Save" });
    this.emailAddressRule = page.getByTestId("email-address");
    this.channelAddressInput = page.getByTestId("field-address-value");
    this.includeField = page.locator("//div[@id='audience-builder-canvas']/div[3]");
    this.activeANDForExclude = page.locator("//button[@color='var(--global-color-background-danger)'][text()='AND']");
    this.activeORForExclude = page.locator("//button[@color='var(--global-color-background-danger)'][text()='OR']");
    this.firstANDForInclude = page.locator(
      "//button[text()='AND'][not(@color='var(--global-color-background-danger)')]"
    );
    this.firstORForInclude = page.locator("//button[text()='OR'][not(@color='var(--global-color-background-danger)')]");
    this.messageAudienceSuccessMessage = page.getByText("Audience has been updated successfully.");
    this.continueButton = page.getByRole("button", { name: "Continue" });
    this.contactsCounterLabel = page.getByTestId("contact-results-counter");
  }

  async getSmsAddressRuleLocator(channelKey: string) {
    return this.page.getByTestId(`${channelKey}-address`);
  }

  async openAudience() {
    await this.audienceEditButton.click();
  }

  async checkAllContacts() {
    await this.allContactsCheckbox.check();
  }

  async uncheckAllContacts() {
    await this.allContactsCheckbox.uncheck();
  }

  async assertSuccessAudiencePopupIsPresent() {
    await expect(this.messageAudienceSuccessMessage).toBeVisible();
    await this.messageAudienceSuccessMessage.click();
  }

  async clickContinue() {
    await this.continueButton.click();
  }

  async assertCounterHasValue(expected: string) {
    await expect(this.contactsCounterLabel).toHaveText(expected);
  }

  async selectRuleEmailAddress() {
    await this.emailAddressRule.click();
  }

  async selectRuleSMSAddress(channelKey: string) {
    const smsAddress = await this.getSmsAddressRuleLocator(channelKey);
    await smsAddress.click();
  }

  async fillAddress(address: string, index = 0) {
    await this.channelAddressInput.nth(index).fill(address);
  }

  async clickSaveButton() {
    await this.saveButton.click();
  }

  async assertAudienceTitleIsPresent() {
    await expect(this.audienceTitle).toBeVisible();
  }

  async selectANDForIncludeSection() {
    await this.firstORForInclude.click();
    await this.firstANDForInclude.click();
  }

  async selectORForIncludeSection() {
    await this.firstANDForInclude.click();
    await this.firstORForInclude.click();
  }

  async selectRulesOperator(rulesOperator: string) {
    if (rulesOperator === ERulesOperator.OR) await this.selectORForIncludeSection();
    if (rulesOperator === ERulesOperator.AND) await this.selectANDForIncludeSection();
  }

  async selectAudienceByAddressRule(address: string[], channelKey: string, rulesOperator?: ERulesOperator | string) {
    console.log("Opening audience");
    await this.openAudience();
    if (channelKey.includes(EChannelKey.EMAIL)) {
      for (let i = 0; i < address.length; i++) {
        await this.selectRuleEmailAddress();
        await this.fillAddress(address[i], i);
      }
    }
    if (isSmsChannelKey(channelKey)) {
      for (let i = 0; i < address.length; i++) {
        await this.selectRuleSMSAddress(channelKey);
        await this.fillAddress(address[i], i);
      }
    }
    if (rulesOperator) await this.selectRulesOperator(rulesOperator);
    await this.clickSaveButton();
    await this.assertSuccessAudiencePopupIsPresent();
    console.log("Message audience information has been saved successfully");
  }
}
