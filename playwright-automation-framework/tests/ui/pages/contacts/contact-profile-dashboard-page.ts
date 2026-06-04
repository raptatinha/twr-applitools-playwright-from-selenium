import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "@pages/base-page";
import { ContactProfileListPage } from "@pages/contacts/contact-profile-lists-page";

export class ContactProfileDashboardPage {
  readonly page: Page;
  readonly locationsTitle: Locator;
  readonly dashboardTabButton: Locator;
  readonly viewAsJsonButton: Locator;
  readonly viewAsJsonModalTitle: Locator;
  readonly viewAsJsonModalCloseButton: Locator;
  readonly eventHistoryTitle: Locator;
  readonly cartItemsTitle: Locator;
  readonly productsOrderedTitle: Locator;
  readonly orderHistoryTitle: Locator;
  readonly supplementsTitle: Locator;
  readonly cartProductItem: Locator;
  readonly orderItem: Locator;
  readonly supplementItem: Locator;
  readonly supplementDropdown: Locator;
  readonly MAX_RETRIES = 10;

  constructor(page: Page) {
    this.page = page;
    this.locationsTitle = page.getByRole("heading", { name: "Locations" });
    this.dashboardTabButton = page.getByRole("tab", { name: "Dashboard" });
    this.viewAsJsonButton = page.getByRole("button", { name: "View as JSON" });
    this.viewAsJsonModalTitle = page.getByTestId("modal-heading");
    this.viewAsJsonModalCloseButton = page.getByRole("button", { name: "Close" });
    this.eventHistoryTitle = page.getByRole("heading", { name: "Event History" });
    this.cartItemsTitle = page.getByRole("heading", { name: "Cart Items" });
    this.productsOrderedTitle = page.getByRole("heading", { name: "Products Ordered" });
    this.orderHistoryTitle = page.getByRole("heading", { name: "Order History" });
    this.supplementsTitle = page.getByRole("heading", { name: "Supplements" });
    this.cartProductItem = page.getByTestId("cart-product-name");
    this.orderItem = page.getByTestId("order-id");
    this.supplementDropdown = page.getByTestId("react-select-value-container").locator("div").nth(1);
    this.supplementItem = page.getByRole("row").getByTestId("supp_name");
  }

  async openContactProfileDashboardTab() {
    console.log("Opening the dashboard tab");
    await this.dashboardTabButton.click();
    this.assertTitleIsPresent();
  }

  async assertTitleIsPresent() {
    await expect(this.locationsTitle).toBeVisible();
  }

  async openViewAsJsonButton() {
    await this.viewAsJsonButton.click();
  }

  async closeViewAsJsonModal() {
    await this.viewAsJsonModalCloseButton.click();
  }

  async assertViewAsJsonModalTitleIsPresent(ID: string) {
    await expect(this.viewAsJsonModalTitle).toBeVisible();
    await expect(this.viewAsJsonModalTitle).toHaveText(ID);
  }

  async selectSupplementInDropdown(supplementName: string) {
    await this.supplementsTitle.scrollIntoViewIfNeeded();
    await this.supplementDropdown.click();
    const supplementOption = this.page.getByText(supplementName);
    await supplementOption.click();
  }

  async assertSupplementItemIsPresent(supplementName: string) {
    await expect(this.supplementItem).toHaveText(supplementName);
  }

  async assertProductItemIsPresent(productName: string) {
    await this.productsOrderedTitle.scrollIntoViewIfNeeded();
    const productItem = this.page.getByText(productName);
    await expect(productItem).toBeVisible();
  }

  async assertEventItemIsPresent(eventName: string) {
    const basePage = new BasePage(this.page);
    const contactProfileListPage = new ContactProfileListPage(this.page);
    await this.eventHistoryTitle.scrollIntoViewIfNeeded();
    const eventItems = this.page.getByText(eventName, { exact: true });
    const item = (await eventItems.count()) <= 1 ? eventItems : eventItems.nth(0);
    await basePage.waitForExpectedValue({
      expectedElement: item,
      expectedValue: eventName,
      maxRetries: this.MAX_RETRIES,
      targetPage: [this.dashboardTabButton],
      proxyPage: contactProfileListPage.listsTabButton,
    });
    await expect(item, `Expected event "${item}" was not found in the Event history`).toBeVisible();
  }

  async assertCartItemIsPresent(items: { name: string }[]) {
    const basePage = new BasePage(this.page);
    const contactProfileListPage = new ContactProfileListPage(this.page);
    const itemsNumber = items.length;
    for (let i = 0; i < itemsNumber; i++) {
      const isItemPresent = await basePage.waitForExpectedValue({
        expectedElement: this.cartProductItem.nth(i),
        expectedValue: items[i].name,
        maxRetries: this.MAX_RETRIES,
        targetPage: [this.dashboardTabButton],
        proxyPage: contactProfileListPage.listsTabButton,
      });
      expect(isItemPresent, `Expected item "${items[i].name}" was not found in the cart`).toBeTruthy();
    }
    const cartNumber = await this.cartProductItem.count();
    expect(cartNumber, "Cart items is empty or incorrect").toBe(itemsNumber);
  }

  async assertOrderItemIsPresent(order: { orderID: string }) {
    const basePage = new BasePage(this.page);
    const contactProfileListPage = new ContactProfileListPage(this.page);
    await this.orderHistoryTitle.scrollIntoViewIfNeeded();
    const isItemPresent = await basePage.waitForExpectedValue({
      expectedElement: this.orderItem,
      expectedValue: order.orderID,
      maxRetries: this.MAX_RETRIES,
      targetPage: [this.dashboardTabButton],
      proxyPage: contactProfileListPage.listsTabButton,
    });
    expect(isItemPresent, `Expected order "${order.orderID}" was not found in the order history`).toBeTruthy();
  }
}
