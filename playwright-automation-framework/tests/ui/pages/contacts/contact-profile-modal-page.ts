import { expect, type Locator, type Page } from "@playwright/test";
import times from "@utils/times";
import { ENavigationMenu } from "@enums/navigation-menu-enum";
import { NavigationMenuPage } from "@pages/general/navigation-menu-page";

export class ContactProfileModalPage {
  readonly page: Page;
  readonly modalTitle: Locator;
  readonly searchInput: Locator;
  readonly contactProfileTitle: Locator;

  constructor(page: Page) {
    this.page = page;
    this.modalTitle = page.getByTestId("modal-heading");
    this.searchInput = page.getByPlaceholder("Search");
    this.contactProfileTitle = page.getByRole("heading", { name: "Contact Profile" });
  }

  async openContactProfileModal() {
    await new NavigationMenuPage(this.page).goToPage(ENavigationMenu.CONTACTS_CONTACT_PROFILE);
  }

  async assertModalTitleIsPresent() {
    await expect(this.modalTitle).toBeVisible();
  }

  async fillInEmail(email: string) {
    await this.searchInput.fill(email);
  }

  async selectContactFromResultList(email: string) {
    await expect(async () => {
      await this.searchInput.clear();
      await this.searchInput.fill(email);
    }, "Contact not found in the list").toPass({ intervals: [times.oneSecond], timeout: times.fiftySeconds });
  }

  async assertProfileTitleIsPresent() {
    await expect(this.contactProfileTitle).toBeVisible();
  }
}
