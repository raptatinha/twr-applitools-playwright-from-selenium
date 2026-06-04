import { expect, type Locator, type Page } from "@playwright/test";

export class LoginPage {
  readonly page: Page;
  readonly userTextBox: Locator;
  readonly passwordTextBox: Locator;
  readonly continueButton: Locator;
  readonly loginButton: Locator;
  readonly errorMessageLabel: Locator;
  readonly welcomeMessageLabel: Locator;
  readonly emailRequiredMessageLabel: Locator;
  readonly passwordRequiredMessageLabel: Locator;

  constructor(page: Page) {
    this.page = page;
    this.userTextBox = page.getByTestId("login-username-input");
    this.passwordTextBox = page.getByTestId("login-password-input");
    this.continueButton = page.getByTestId("login-continue-button");
    this.loginButton = page.getByTestId("login-button");
    this.errorMessageLabel = page.getByText("Login invalid. Please verify your credentials.");
    this.welcomeMessageLabel = page.getByText("Welcome back");
    this.emailRequiredMessageLabel = page.getByText("Email is required");
    this.passwordRequiredMessageLabel = page.getByText("Password is required");
  }

  async doLogin(userName: string, password: string) {
    await this.fillUserName(userName);
    await this.clickContinueButton();
    await this.fillPassword(password);
    await this.clickLoginButton();
  }

  async fillUserName(userName: string) {
    await this.userTextBox.fill(userName);
  }

  async fillPassword(password: string) {
    await this.passwordTextBox.fill(password);
  }

  async clickContinueButton() {
    await this.continueButton.click();
  }

  async clickLoginButton() {
    await this.loginButton.click();
  }

  async assertErrorMessageIsPresent() {
    await expect(this.errorMessageLabel).toBeVisible();
  }

  async assertWelcomeMessageIsDisplayed() {
    await expect(this.welcomeMessageLabel).toBeVisible();
  }

  async assertEmailRequiredMessageIsDisplayed() {
    await expect(this.emailRequiredMessageLabel).toBeVisible();
  }

  async assertPasswordRequiredMessageIsDisplayed() {
    await expect(this.passwordRequiredMessageLabel).toBeVisible();
  }
}
