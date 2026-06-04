import { getCredentials } from "@setup/base-env-setup";
import { test } from "@setup/base-fixture";
import userData from "@data/user-data";

const credentials = getCredentials()!;

test.use({ storageState: { cookies: [], origins: [] } }); // doesn't share the logged in session

test.describe("Login suite", { tag: ["@login"] }, () => {
  test("Login with valid credentials and logout", async ({ pm }) => {
    await test.step("Login and validate dashboard is loaded", async () => {
      await pm.getLoginPage().doLogin(credentials.username, credentials.password);
      await pm.getDashboardPage().assertDashboardTitleIsPresent();
    });
    await test.step("Logout and validate dashboard is loaded", async () => {
      await pm.getTopMenuPage().doLogout();
      await pm.getLoginPage().assertWelcomeMessageIsDisplayed();
    });
  });

  test("Login with invalid credentials", async ({ pm }) => {
    await test.step("Login and validate error message", async () => {
      await pm.getLoginPage().doLogin(userData.invalidEmail, userData.invalidPassword);
      await pm.getLoginPage().assertErrorMessageIsPresent();
    });
  });

  test("Validate Email and password are required to log in the account", async ({ pm }) => {
    await test.step("Continue without email", async () => {
      await pm.getLoginPage().clickContinueButton();
      await pm.getLoginPage().assertEmailRequiredMessageIsDisplayed();
    });
    await test.step("Fill email and try to login without password", async () => {
      await pm.getLoginPage().fillUserName(credentials.username);
      await pm.getLoginPage().clickContinueButton();
      await pm.getLoginPage().clickLoginButton();
      await pm.getLoginPage().assertPasswordRequiredMessageIsDisplayed();
    });
  });
});
