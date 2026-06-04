package com.acmeplatform.automation.pages;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * DashboardPage - Post-login dashboard verification and navigation.
 */
public class DashboardPage extends BasePage {

    private static final By DASHBOARD_HEADER = By.cssSelector("[data-auto-qa='dashboard-header']");
    private static final By WELCOME_MESSAGE = By.cssSelector(".welcome-message");
    private static final By NAVIGATION_MENU = By.cssSelector(".main-navigation, .side-nav");
    private static final By USER_AVATAR = By.cssSelector(".user-avatar, .profile-icon");
    private static final By LOGOUT_BUTTON = By.cssSelector("[data-auto-qa='logout-button']");
    private static final By USER_DROPDOWN = By.cssSelector(".user-dropdown-menu");
    private static final By CONTACTS_MENU_ITEM = By.cssSelector("[data-auto-qa='nav-contacts']");
    private static final By MESSAGES_MENU_ITEM = By.cssSelector("[data-auto-qa='nav-messages']");
    private static final By DATA_JOBS_MENU_ITEM = By.cssSelector("[data-auto-qa='nav-data-jobs']");
    private static final By ACCOUNT_NAME = By.cssSelector(".account-name");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public void verifyDashboardLoaded() {
        logger.info("Verifying dashboard is loaded");
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(3000); // Dashboard has many async widgets loading
        WaitUtils.waitForElementVisible(driver, DASHBOARD_HEADER);
        WaitUtils.waitForElementVisible(driver, NAVIGATION_MENU);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Dashboard loaded successfully");
    }

    public String getWelcomeMessage() {
        WaitUtils.waitForElementVisible(driver, WELCOME_MESSAGE);
        WaitUtils.hardWait(500);
        return driver.findElement(WELCOME_MESSAGE).getText();
    }

    public void logout() {
        logger.info("Performing logout");
        WaitUtils.waitForElementClickable(driver, USER_AVATAR);
        WaitUtils.hardWait(500);
        click(USER_AVATAR);
        WaitUtils.hardWait(1000); // Wait for dropdown animation
        WaitUtils.waitForElementVisible(driver, USER_DROPDOWN);
        WaitUtils.waitForElementClickable(driver, LOGOUT_BUTTON);
        WaitUtils.hardWait(300);
        click(LOGOUT_BUTTON);
        WaitUtils.hardWait(2000); // Wait for logout redirect
        WaitUtils.waitForPageLoad(driver);
        ReportManager.logStep("Logged out successfully");
    }

    public void navigateToContacts() {
        logger.info("Navigating to Contacts");
        WaitUtils.waitForElementClickable(driver, CONTACTS_MENU_ITEM);
        WaitUtils.hardWait(500);
        click(CONTACTS_MENU_ITEM);
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Navigated to Contacts page");
    }

    public void navigateToMessages() {
        logger.info("Navigating to Messages");
        WaitUtils.waitForElementClickable(driver, MESSAGES_MENU_ITEM);
        WaitUtils.hardWait(500);
        click(MESSAGES_MENU_ITEM);
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Navigated to Messages page");
    }

    public void navigateToDataJobs() {
        logger.info("Navigating to Data Jobs");
        WaitUtils.waitForElementClickable(driver, DATA_JOBS_MENU_ITEM);
        WaitUtils.hardWait(500);
        click(DATA_JOBS_MENU_ITEM);
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Navigated to Data Jobs page");
    }

    public String getAccountName() {
        WaitUtils.waitForElementVisible(driver, ACCOUNT_NAME);
        return driver.findElement(ACCOUNT_NAME).getText();
    }

    public boolean isDashboardDisplayed() {
        try {
            WaitUtils.waitForElementVisible(driver, DASHBOARD_HEADER, 10);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
