import { type Locator, type Page } from "@playwright/test";
import { ENavigationMenu } from "@enums/navigation-menu-enum";

export class NavigationMenuPage {
  readonly navigationMap = new Map<ENavigationMenu, Locator>();

  readonly dashboardSection: Locator;
  private readonly ordersSection: Locator;
  private readonly productsSection: Locator;
  private readonly customersSection: Locator;
  private readonly reportsSection: Locator;
  private readonly settingsSection: Locator;

  constructor(page: Page) {
    this.navigationMap.set(ENavigationMenu.DASHBOARD, page.getByTestId("nav-item-dashboard"));

    this.dashboardSection = page.getByTestId("nav-item-dashboard");
    this.ordersSection = page.getByTestId("nav-item-orders");
    this.productsSection = page.getByTestId("nav-item-products");
    this.customersSection = page.getByTestId("nav-item-customers");
    this.reportsSection = page.getByTestId("nav-item-reports");
    this.settingsSection = page.getByTestId("nav-item-settings");
  }

  public async goToPage(navigationMenu: ENavigationMenu) {
    const sections = {
      ORDERS_: this.ordersSection,
      PRODUCTS_: this.productsSection,
      CUSTOMERS_: this.customersSection,
      REPORTS_: this.reportsSection,
      SETTINGS_: this.settingsSection,
    };
    for (const prefix in sections) {
      if (navigationMenu.startsWith(prefix)) {
        await sections[prefix].click();
        break;
      }
    }
    const navigationItem = this.navigationMap.get(navigationMenu);
    if (navigationItem) {
      await navigationItem.click();
    } else {
      throw new Error(`Navigation menu item ${navigationMenu} not found.`);
    }
  }
}
