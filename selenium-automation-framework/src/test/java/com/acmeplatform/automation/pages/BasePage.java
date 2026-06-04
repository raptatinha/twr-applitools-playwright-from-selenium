package com.acmeplatform.automation.pages;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * BasePage - Abstract base class for all page objects.
 * 
 * Pain points demonstrated:
 * - Every method wraps an action with explicit waits
 * - 20+ common methods that all page objects inherit (deep coupling)
 * - Duplicated wait logic in every interaction method
 * - Thread.sleep sprinkled as "safety nets"
 * - Manual scroll handling, manual JS execution
 * - Tight coupling to WaitUtils static methods
 */
public abstract class BasePage {

    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    protected WebDriver driver;
    protected Actions actions;

    // Common locators duplicated in base
    protected static final By LOADING_SPINNER = By.cssSelector(".loading-spinner, .spinner-overlay");
    protected static final By SUCCESS_TOAST = By.cssSelector(".toast.toast-success");
    protected static final By ERROR_TOAST = By.cssSelector(".toast.toast-error");
    protected static final By MODAL_CONFIRM_BTN = By.cssSelector("[data-auto-qa='alert--action--confirm']");
    protected static final By MODAL_CANCEL_BTN = By.cssSelector("[data-auto-qa='alert--action--cancel']");
    protected static final By MODAL_CLOSE_ICON = By.cssSelector("[data-auto-qa='alert-modal--button--close']");

    private static final int ELEMENT_TIMEOUT = 20;
    private static final int SHORT_WAIT = 500;
    private static final int MEDIUM_WAIT = 1000;
    private static final int LONG_WAIT = 3000;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // ==================== Navigation ====================

    public void openUrl(String url) {
        logger.info("Opening URL: " + url);
        driver.get(url);
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(MEDIUM_WAIT);
        ReportManager.logStep("Opened URL: " + url);
    }

    public void navigateBack() {
        driver.navigate().back();
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    public void refreshPage() {
        driver.navigate().refresh();
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(MEDIUM_WAIT);
    }

    // ==================== Click Operations ====================

    public void click(By locator) {
        logger.debug("Clicking element: " + locator);
        WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(300); // Buffer before click
        WebElement element = WaitUtils.waitForElementClickable(driver, locator);
        element.click();
        WaitUtils.hardWait(SHORT_WAIT); // Buffer after click
    }

    public void clickWithRetry(By locator) {
        logger.debug("Click with retry: " + locator);
        int attempts = 0;
        while (attempts < 3) {
            try {
                WaitUtils.waitForElementVisible(driver, locator);
                WaitUtils.hardWait(300);
                WebElement element = WaitUtils.waitForElementClickable(driver, locator);
                element.click();
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                attempts++;
                logger.warn("Click attempt " + attempts + " failed: " + e.getMessage());
                WaitUtils.hardWait(MEDIUM_WAIT);
            }
        }
        throw new RuntimeException("Failed to click element after 3 attempts: " + locator);
    }

    public void clickByJs(By locator) {
        logger.debug("JS click: " + locator);
        WebElement element = WaitUtils.waitForElementPresent(driver, locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    public void doubleClick(By locator) {
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(300);
        actions.doubleClick(element).perform();
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Input Operations ====================

    public void type(By locator, String text) {
        logger.debug("Typing '" + text + "' into: " + locator);
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(300);
        element.clear();
        WaitUtils.hardWait(200);
        element.sendKeys(text);
        WaitUtils.hardWait(300);
    }

    public void typeSlowly(By locator, String text) {
        logger.debug("Typing slowly into: " + locator);
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            WaitUtils.hardWait(100);
        }
    }

    public void clearAndType(By locator, String text) {
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(300);
        element.sendKeys(Keys.CONTROL + "a");
        WaitUtils.hardWait(100);
        element.sendKeys(Keys.DELETE);
        WaitUtils.hardWait(200);
        element.sendKeys(text);
        WaitUtils.hardWait(300);
    }

    public void selectDropdownByText(By locator, String visibleText) {
        logger.debug("Selecting dropdown '" + visibleText + "' from: " + locator);
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(SHORT_WAIT);
        Select select = new Select(element);
        select.selectByVisibleText(visibleText);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    public void selectDropdownByValue(By locator, String value) {
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        WaitUtils.hardWait(SHORT_WAIT);
        Select select = new Select(element);
        select.selectByValue(value);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Assertions / Verification ====================

    public boolean isElementDisplayed(By locator) {
        try {
            return WaitUtils.waitForElementVisible(driver, locator, 5).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getElementText(By locator) {
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        return element.getText();
    }

    public String getElementAttribute(By locator, String attribute) {
        WebElement element = WaitUtils.waitForElementPresent(driver, locator);
        return element.getAttribute(attribute);
    }

    public void verifyElementText(By locator, String expectedText) {
        WaitUtils.waitForTextPresent(driver, locator, expectedText);
        String actualText = getElementText(locator);
        if (!actualText.contains(expectedText)) {
            throw new AssertionError("Expected text '" + expectedText + "' but found '" + actualText + "'");
        }
        ReportManager.logStep("Verified text: " + expectedText);
    }

    // ==================== Wait & Toast Handling ====================

    public void waitForSuccessToast() {
        logger.debug("Waiting for success toast");
        WaitUtils.waitForElementVisible(driver, SUCCESS_TOAST);
        WaitUtils.hardWait(MEDIUM_WAIT); // Let toast animation complete
        try {
            driver.findElement(SUCCESS_TOAST).click(); // Dismiss toast
        } catch (Exception e) {
            // Toast may auto-dismiss
        }
        WaitUtils.waitForElementInvisible(driver, SUCCESS_TOAST);
    }

    public void waitForLoadingSpinnerToDisappear() {
        logger.debug("Waiting for loading spinner to disappear");
        try {
            if (isElementPresent(LOADING_SPINNER)) {
                WaitUtils.waitForElementInvisible(driver, LOADING_SPINNER, 30);
            }
        } catch (Exception e) {
            // Spinner may have already disappeared
        }
        WaitUtils.hardWait(SHORT_WAIT);
    }

    public void confirmAlertModal() {
        logger.debug("Confirming alert modal");
        WaitUtils.waitForElementVisible(driver, MODAL_CONFIRM_BTN);
        WaitUtils.hardWait(SHORT_WAIT);
        click(MODAL_CONFIRM_BTN);
        WaitUtils.hardWait(MEDIUM_WAIT);
    }

    public void dismissAlertModal() {
        WaitUtils.waitForElementVisible(driver, MODAL_CANCEL_BTN);
        click(MODAL_CANCEL_BTN);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Scroll Operations ====================

    public void scrollToElement(By locator) {
        WebElement element = WaitUtils.waitForElementPresent(driver, locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        WaitUtils.hardWait(MEDIUM_WAIT); // Wait for scroll animation
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        WaitUtils.hardWait(MEDIUM_WAIT);
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Hover & Drag ====================

    public void hoverElement(By locator) {
        WebElement element = WaitUtils.waitForElementVisible(driver, locator);
        actions.moveToElement(element).perform();
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Window/Tab Handling ====================

    public void switchToNewWindow() {
        String mainWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(MEDIUM_WAIT);
    }

    public void switchToMainWindow() {
        String mainWindow = driver.getWindowHandles().iterator().next();
        driver.switchTo().window(mainWindow);
        WaitUtils.hardWait(SHORT_WAIT);
    }

    // ==================== Wait for Expected Value (Polling) ====================

    /**
     * Polls for a value by refreshing the page - a pattern that shows how much manual
     * work is needed without Playwright's auto-retrying assertions.
     */
    public boolean waitForExpectedValue(By locator, String expectedValue, int maxRetries) {
        logger.info("Waiting for value '" + expectedValue + "' with " + maxRetries + " retries");
        for (int i = 0; i < maxRetries; i++) {
            try {
                WebElement element = WaitUtils.waitForElementVisible(driver, locator, 10);
                String actualValue = element.getText();
                if (actualValue.contains(expectedValue)) {
                    logger.info("Expected value found after " + (i + 1) + " attempts");
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Attempt " + (i + 1) + ": element not ready");
            }
            logger.info("Attempt " + (i + 1) + "/" + maxRetries + " - value not found, refreshing...");
            WaitUtils.hardWait(LONG_WAIT);
            refreshPage();
            waitForLoadingSpinnerToDisappear();
        }
        logger.error("Value '" + expectedValue + "' not found after " + maxRetries + " attempts");
        return false;
    }
}
