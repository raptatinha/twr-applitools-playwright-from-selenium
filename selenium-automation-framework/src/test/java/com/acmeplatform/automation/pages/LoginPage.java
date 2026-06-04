package com.acmeplatform.automation.pages;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * LoginPage - Handles authentication flows.
 * 
 * ============================================================================================
 * SELENIUM LOGIN vs PLAYWRIGHT LOGIN:
 *
 * In Selenium, every test must either:
 *   a) Login via UI (slow: 3-5 seconds per test), or
 *   b) Use a separate cookie injection mechanism (complex, brittle)
 *
 * In Playwright:
 *   // global-setup.ts (runs ONCE before all tests)
 *   const context = await browser.newContext();
 *   const page = await context.newPage();
 *   await page.goto('/login');
 *   await page.fill('#email', user);
 *   await page.fill('#password', pass);
 *   await page.click('button[type=submit]');
 *   await context.storageState({ path: 'session.json' });
 *
 *   // Every test reuses the session:
 *   use: { storageState: 'session.json' }
 *
 * Result: Login happens ONCE. All tests start already authenticated.
 * Saves 3-5 seconds per test. In a suite of 100 tests = 5-8 minutes saved.
 *
 * ALSO NOTE: Look at all the Thread.sleep calls below.
 * - enterEmail: wait 500ms before, 200ms after clear, 300ms after type
 * - enterPassword: same pattern
 * - clickLoginButton: wait 500ms before click, 3000ms after for redirect
 * - login(): 2000ms additional wait at the end
 *
 * TOTAL: ~7 seconds of Thread.sleep PER LOGIN.
 * In Playwright: ~0.5 seconds total (auto-waits, no sleeps).
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Thread.sleep between every step (no auto-waiting)
 * - Explicit waits even for simple form interactions
 * - Manual page load verification
 */
public class LoginPage extends BasePage {

    // Locators - using multiple selector strategies (inconsistent)
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By LOGIN_FORM = By.cssSelector("form.login-form");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message, .alert-danger");
    private static final By FORGOT_PASSWORD_LINK = By.linkText("Forgot Password?");
    private static final By EMAIL_ERROR = By.cssSelector("#email-error, .email-validation-error");
    private static final By PASSWORD_ERROR = By.cssSelector("#password-error, .password-validation-error");
    private static final By REMEMBER_ME_CHECKBOX = By.id("remember-me");
    private static final By LOGO = By.cssSelector(".login-logo, .brand-logo");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void verifyLoginPageLoaded() {
        logger.info("Verifying login page is loaded");
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000); // Wait for page animations
        WaitUtils.waitForElementVisible(driver, LOGIN_FORM);
        WaitUtils.waitForElementVisible(driver, EMAIL_INPUT);
        WaitUtils.waitForElementVisible(driver, PASSWORD_INPUT);
        WaitUtils.waitForElementVisible(driver, LOGIN_BUTTON);
        ReportManager.logStep("Login page loaded successfully");
    }

    public void enterEmail(String email) {
        logger.info("Entering email: " + email);
        WaitUtils.waitForElementVisible(driver, EMAIL_INPUT);
        WaitUtils.hardWait(500); // Wait for input to be interactive
        WebElement emailField = WaitUtils.waitForElementClickable(driver, EMAIL_INPUT);
        emailField.clear();
        WaitUtils.hardWait(200);
        emailField.sendKeys(email);
        WaitUtils.hardWait(300); // Wait for validation
        ReportManager.logStep("Entered email: " + email);
    }

    public void enterPassword(String password) {
        logger.info("Entering password");
        WaitUtils.waitForElementVisible(driver, PASSWORD_INPUT);
        WaitUtils.hardWait(500);
        WebElement passwordField = WaitUtils.waitForElementClickable(driver, PASSWORD_INPUT);
        passwordField.clear();
        WaitUtils.hardWait(200);
        passwordField.sendKeys(password);
        WaitUtils.hardWait(300);
        ReportManager.logStep("Entered password");
    }

    public void clickLoginButton() {
        logger.info("Clicking login button");
        WaitUtils.waitForElementClickable(driver, LOGIN_BUTTON);
        WaitUtils.hardWait(500); // Extra wait before click
        driver.findElement(LOGIN_BUTTON).click();
        WaitUtils.hardWait(3000); // Wait for login API call and redirect
        WaitUtils.waitForPageLoad(driver);
        ReportManager.logStep("Clicked login button");
    }

    public void login(String email, String password) {
        logger.info("Performing login with email: " + email);
        verifyLoginPageLoaded();
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
        // Additional wait for dashboard to load after login
        WaitUtils.hardWait(2000);
        logger.info("Login completed");
    }

    public String getErrorMessage() {
        WaitUtils.waitForElementVisible(driver, ERROR_MESSAGE, 10);
        WaitUtils.hardWait(500);
        return driver.findElement(ERROR_MESSAGE).getText();
    }

    public boolean isErrorMessageDisplayed() {
        try {
            WaitUtils.waitForElementVisible(driver, ERROR_MESSAGE, 5);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmailErrorDisplayed() {
        try {
            WaitUtils.hardWait(1000); // Wait for validation to trigger
            return driver.findElement(EMAIL_ERROR).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordErrorDisplayed() {
        try {
            WaitUtils.hardWait(1000);
            return driver.findElement(PASSWORD_ERROR).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickForgotPassword() {
        WaitUtils.waitForElementClickable(driver, FORGOT_PASSWORD_LINK);
        WaitUtils.hardWait(300);
        driver.findElement(FORGOT_PASSWORD_LINK).click();
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(1000);
    }

    public void checkRememberMe() {
        WaitUtils.waitForElementClickable(driver, REMEMBER_ME_CHECKBOX);
        WaitUtils.hardWait(300);
        WebElement checkbox = driver.findElement(REMEMBER_ME_CHECKBOX);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        WaitUtils.hardWait(300);
    }
}
