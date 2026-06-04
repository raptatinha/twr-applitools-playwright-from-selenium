import { expect, type Locator, type Page } from "@playwright/test";
import { ESubscriptionStatus } from "@enums/contact-enum";
import { ENavigationMenu } from "@enums/navigation-menu-enum";
import { NavigationMenuPage } from "@pages/general/navigation-menu-page";

export class ContactCreatePage {
  readonly page: Page;
  readonly title: Locator;
  readonly saveButton: Locator;
  readonly ConfirmationMessagePopup: Locator;
  readonly channelsEmailInput: Locator;
  readonly channelsEmailSubscribeStatusCheckbox: Locator;
  readonly channelsEmailUnsubscribeStatusCheckbox: Locator;
  readonly channelsEmailNoneStatusCheckbox: Locator;

  constructor(page: Page) {
    this.page = page;
    this.title = page.getByRole("heading", { name: "Create Contact" });
    this.channelsEmailInput = page.getByTestId("channel-email-value").getByRole("textbox");
    this.channelsEmailSubscribeStatusCheckbox = page
      .getByTestId("field-channels[0].subscribeStatus:subscribed")
      .getByLabel("Subscribed");
    this.channelsEmailUnsubscribeStatusCheckbox = page
      .getByTestId("field-channels[0].subscribeStatus:unsubscribed")
      .getByLabel("Unsubscribed");
    this.channelsEmailNoneStatusCheckbox = page
      .getByTestId("field-channels[0].subscribeStatus:none")
      .getByLabel("None");
    this.saveButton = page.getByRole("button", { name: "Save" });
    this.ConfirmationMessagePopup = page.getByText("Contact has been added");
  }

  async openCreateContactForm() {
    await new NavigationMenuPage(this.page).goToPage(ENavigationMenu.CONTACTS_CREATE_CONTACT);
  }

  async fillEmail(email: string) {
    console.log(`Filling email address: ${email}`);
    await this.channelsEmailInput.fill(email);
  }

  async selectSubscribeStatus(status: string) {
    console.log(`Selecting subscribe status: ${status}`);
    switch (status) {
      case ESubscriptionStatus.SUBSCRIBED:
        await this.channelsEmailSubscribeStatusCheckbox.check();
        break;
      case ESubscriptionStatus.UNSUBSCRIBED:
        await this.channelsEmailUnsubscribeStatusCheckbox.check();
        break;
      case ESubscriptionStatus.NONE:
        await this.channelsEmailNoneStatusCheckbox.check();
        break;
    }
  }

  async clickSaveButton() {
    await this.saveButton.click();
  }

  async assertTitleIsPresent() {
    await expect(this.title).toBeVisible();
  }

  async assertConfirmationMessagePopupIsPresent() {
    await expect(this.ConfirmationMessagePopup).toBeVisible();
    await this.ConfirmationMessagePopup.click();
  }
}
