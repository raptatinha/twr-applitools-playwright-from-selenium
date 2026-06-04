import { type Page } from "@playwright/test";
import { AudiencePage } from "@pages/audience/audience-page";
import { BasePage } from "@pages/base-page";
import { ContactsStepsPage } from "@pages/contacts/contact-steps-page";
import { DashboardPage } from "@pages/general/dashboard-page";
import { LoginPage } from "@pages/general/login-page";
import { NavigationMenuPage } from "@pages/general/navigation-menu-page";
import { TopMenuPage } from "@pages/general/top-menu-page";
import { MessageStepsPage } from "@pages/message/message-steps-page";

export class PageManager {
  readonly page: Page;
  private readonly basePage: BasePage;
  private readonly audiencePage: AudiencePage;
  private readonly contactsStepsPage: ContactsStepsPage;
  private readonly messageStepsPage: MessageStepsPage;
  private readonly dashboardPage: DashboardPage;
  private readonly loginPage: LoginPage;
  private readonly topMenuPage: TopMenuPage;
  private readonly navigationMenuPage: NavigationMenuPage;

  constructor(page: Page) {
    this.page = page;
    this.basePage = new BasePage(this.page);
    this.audiencePage = new AudiencePage(this.page);
    this.contactsStepsPage = new ContactsStepsPage(this.page);
    this.messageStepsPage = new MessageStepsPage(this.page);
    this.dashboardPage = new DashboardPage(this.page);
    this.loginPage = new LoginPage(this.page);
    this.topMenuPage = new TopMenuPage(this.page);
    this.navigationMenuPage = new NavigationMenuPage(this.page);
  }

  getBasePage() {
    return this.basePage;
  }

  getAudiencePage() {
    return this.audiencePage;
  }

  getContactsStepsPage() {
    return this.contactsStepsPage;
  }

  getMessageStepsPage() {
    return this.messageStepsPage;
  }

  getDashboardPage() {
    return this.dashboardPage;
  }

  getLoginPage() {
    return this.loginPage;
  }

  getTopMenuPage() {
    return this.topMenuPage;
  }

  getNavigationMenuPage() {
    return this.navigationMenuPage;
  }
}
