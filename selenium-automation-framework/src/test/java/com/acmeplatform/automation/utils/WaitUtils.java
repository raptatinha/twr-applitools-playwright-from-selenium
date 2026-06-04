package com.acmeplatform.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * WaitUtils - Collection of explicit wait methods.
 * 
 * ============================================================================================
 * THIS FILE IS THE #1 PAIN POINT IN SELENIUM.
 *
 * In Playwright, this entire 200+ line file DOES NOT EXIST.
 * Every Playwright action (click, fill, check, etc.) auto-waits for:
 *   - Element to be attached to DOM
 *   - Element to be visible
 *   - Element to be stable (not animating)
 *   - Element to receive events (not obscured)
 *   - Element to be enabled
 *
 * With Selenium, the developer must manually decide:
 *   - Which wait to use (explicit vs implicit vs fluent vs Thread.sleep)
 *   - What condition to wait for (visible? clickable? present? text?)
 *   - How long to wait (5s? 20s? 60s?)
 *   - What to do if the wait fails (retry? refresh? give up?)
 *   - Whether to add a "safety buffer" Thread.sleep after the wait
 *
 * The result: flaky tests that pass locally but fail in CI due to timing,
 * or tests bloated with 3-5 second sleeps that make the suite take forever.
 *
 * PLAYWRIGHT EQUIVALENT:
 *   await page.click('#button');       // Auto-waits for clickable
 *   await page.fill('#input', 'text'); // Auto-waits for editable
 *   await expect(locator).toBeVisible(); // Auto-retries for 5s
 *   await expect(locator).toHaveText('Done'); // Auto-retries
 *
 * That's it. No WaitUtils. No Thread.sleep. No timing decisions.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - 15+ different wait methods that all do slightly different things
 * - Hardcoded timeout values duplicated across methods
 * - Thread.sleep used as fallback when explicit waits "don't work"
 * - Mix of WebDriverWait, FluentWait, and raw Thread.sleep
 * - Every page object method needs to call one of these
 * - No auto-waiting - developer must choose the right wait for each action
 */
public class WaitUtils {

    private static final Logger logger = LogManager.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 20;
    private static final int SHORT_TIMEOUT = 5;
    private static final int LONG_TIMEOUT = 60;
    private static final int POLLING_INTERVAL = 500;

    // ==================== Element Visibility Waits ====================

    public static WebElement waitForElementVisible(WebDriver driver, By locator) {
        logger.debug("Waiting for element to be visible: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElementVisible(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Waiting for element to be visible (timeout: " + timeoutSeconds + "s): " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElementVisible(WebDriver driver, WebElement element) {
        logger.debug("Waiting for WebElement to be visible");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public static List<WebElement> waitForAllElementsVisible(WebDriver driver, By locator) {
        logger.debug("Waiting for all elements to be visible: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ==================== Element Clickable Waits ====================

    public static WebElement waitForElementClickable(WebDriver driver, By locator) {
        logger.debug("Waiting for element to be clickable: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForElementClickable(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Waiting for element to be clickable (timeout: " + timeoutSeconds + "s): " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForElementClickable(WebDriver driver, WebElement element) {
        logger.debug("Waiting for WebElement to be clickable");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    // ==================== Element Presence Waits ====================

    public static WebElement waitForElementPresent(WebDriver driver, By locator) {
        logger.debug("Waiting for element to be present in DOM: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static List<WebElement> waitForAllElementsPresent(WebDriver driver, By locator) {
        logger.debug("Waiting for all elements to be present: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ==================== Text & Attribute Waits ====================

    public static boolean waitForTextPresent(WebDriver driver, By locator, String text) {
        logger.debug("Waiting for text '" + text + "' in element: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static boolean waitForTextPresent(WebDriver driver, By locator, String text, int timeoutSeconds) {
        logger.debug("Waiting for text '" + text + "' (timeout: " + timeoutSeconds + "s): " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static boolean waitForAttributeContains(WebDriver driver, By locator, String attribute, String value) {
        logger.debug("Waiting for attribute '" + attribute + "' to contain '" + value + "': " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    // ==================== Invisibility & Staleness Waits ====================

    public static boolean waitForElementInvisible(WebDriver driver, By locator) {
        logger.debug("Waiting for element to be invisible: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static boolean waitForElementInvisible(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Waiting for element to be invisible (timeout: " + timeoutSeconds + "s): " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static boolean waitForElementStale(WebDriver driver, WebElement element) {
        logger.debug("Waiting for element to become stale");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.stalenessOf(element));
    }

    // ==================== Page & Frame Waits ====================

    public static void waitForPageLoad(WebDriver driver) {
        logger.debug("Waiting for page to fully load...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(LONG_TIMEOUT));
        wait.until((ExpectedCondition<Boolean>) d ->
                ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete")
        );
        // Additional wait because readyState doesn't guarantee all AJAX calls are done
        hardWait(1000);
    }

    public static void waitForAjaxComplete(WebDriver driver) {
        logger.debug("Waiting for all AJAX calls to complete...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(LONG_TIMEOUT));
        wait.until((ExpectedCondition<Boolean>) d -> {
            JavascriptExecutor js = (JavascriptExecutor) d;
            return (Boolean) js.executeScript(
                    "return (typeof jQuery !== 'undefined') ? jQuery.active == 0 : true"
            );
        });
    }

    public static void waitForAngularLoad(WebDriver driver) {
        logger.debug("Waiting for Angular to finish loading...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(LONG_TIMEOUT));
        wait.until((ExpectedCondition<Boolean>) d -> {
            JavascriptExecutor js = (JavascriptExecutor) d;
            return (Boolean) js.executeScript(
                    "return (window.angular !== undefined) && " +
                            "(angular.element(document).injector() !== undefined) && " +
                            "(angular.element(document).injector().get('$http').pendingRequests.length === 0)"
            );
        });
    }

    public static WebDriver waitForFrameAndSwitch(WebDriver driver, By frameLocator) {
        logger.debug("Waiting for frame and switching: " + frameLocator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    // ==================== URL & Title Waits ====================

    public static boolean waitForUrlContains(WebDriver driver, String urlFragment) {
        logger.debug("Waiting for URL to contain: " + urlFragment);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    public static boolean waitForTitleContains(WebDriver driver, String title) {
        logger.debug("Waiting for title to contain: " + title);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        return wait.until(ExpectedConditions.titleContains(title));
    }

    // ==================== Fluent Wait ====================

    public static WebElement fluentWaitForElement(WebDriver driver, By locator) {
        logger.debug("Fluent wait for element: " + locator);
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(LONG_TIMEOUT))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementNotInteractableException.class);

        return wait.until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return driver.findElement(locator);
            }
        });
    }

    public static WebElement fluentWaitForElementClickable(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Fluent wait for element clickable (timeout: " + timeoutSeconds + "s): " + locator);
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ==================== Hard Wait (Thread.sleep) ====================

    /**
     * Use this when explicit waits don't work reliably.
     * Known scenarios: after AJAX calls, after page transitions, before interacting with dynamic elements.
     */
    public static void hardWait(long milliseconds) {
        try {
            logger.debug("Hard wait: " + milliseconds + "ms");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Hard wait interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wait with retry - keeps trying to find an element with small delays between attempts
     */
    public static WebElement waitWithRetry(WebDriver driver, By locator, int maxAttempts) {
        logger.debug("Wait with retry (attempts: " + maxAttempts + "): " + locator);
        WebElement element = null;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                logger.debug("Attempt " + (attempt + 1) + " failed, retrying...");
            }
            hardWait(1000);
            attempt++;
        }

        throw new TimeoutException("Element not found after " + maxAttempts + " attempts: " + locator);
    }

    // ==================== Combined Waits ====================

    /**
     * Wait for element visible and then click it - combining two wait types
     * because sometimes waitForClickable alone doesn't work
     */
    public static void waitAndClick(WebDriver driver, By locator) {
        logger.debug("Wait and click: " + locator);
        waitForElementVisible(driver, locator);
        hardWait(500); // Small buffer after visibility
        waitForElementClickable(driver, locator).click();
    }

    /**
     * Wait for element, clear it, and type text - with waits between each action
     */
    public static void waitClearAndType(WebDriver driver, By locator, String text) {
        logger.debug("Wait, clear and type in: " + locator);
        WebElement element = waitForElementVisible(driver, locator);
        hardWait(300); // Buffer before interaction
        element.clear();
        hardWait(200); // Buffer after clear
        element.sendKeys(text);
    }

    /**
     * Scroll to element and wait for it to be visible
     */
    public static WebElement scrollAndWaitForElement(WebDriver driver, By locator) {
        logger.debug("Scrolling to element: " + locator);
        WebElement element = waitForElementPresent(driver, locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        hardWait(500); // Wait for scroll animation
        return waitForElementVisible(driver, locator);
    }
}
