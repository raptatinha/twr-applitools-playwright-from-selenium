package com.acmeplatform.automation.base;

import com.acmeplatform.automation.utils.ConfigReader;
import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * BaseTest - All test classes must extend this.
 * 
 * ============================================================================================
 * THIS IS 160+ LINES OF INFRASTRUCTURE CODE.
 *
 * In Playwright, the equivalent is:
 *   - playwright.config.ts (shared config, ~30 lines)
 *   - base-fixture.ts (custom fixtures, ~25 lines)
 *
 * That's it. 55 lines vs 160 lines. And it's cleaner.
 *
 * What BaseTest does manually that Playwright handles automatically:
 *   1. Browser lifecycle (@BeforeSuite/@AfterSuite) → playwright.config.ts
 *   2. Test reporting initialization → built-in HTML reporter
 *   3. Screenshot on failure → config: screenshot: 'only-on-failure'
 *   4. Test method logging → trace: 'retain-on-failure'
 *   5. Page navigation → fixture with auto-navigation
 *   6. Cleanup/teardown → automatic browser context disposal
 *
 * PROBLEMS WITH THIS PATTERN:
 *   - Every test class MUST extend BaseTest (tight coupling)
 *   - Adding a new setup step means modifying BaseTest (breaks Open/Closed principle)
 *   - Thread.sleep(3000) in @BeforeMethod adds 3s to EVERY test
 *   - Can't compose behaviors - only single inheritance in Java
 *   - If BaseTest has a bug, ALL tests are affected
 *   - Hard to understand test execution order (inheritance chain)
 *
 * PLAYWRIGHT ALTERNATIVE:
 *   test.extend() allows composing multiple fixtures independently.
 *   Each fixture is opt-in, not forced by inheritance.
 *   No global mutable state. No Thread.sleep in setup.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Every test class inherits a massive base with setup/teardown logic
 * - Manual screenshot capture on failure
 * - Manual report initialization/flush
 * - Driver lifecycle management scattered between base and factory
 * - Hard to understand test execution flow (inheritance chain)
 * - Tight coupling between test infrastructure and test logic
 */
public class BaseTest {

    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected String baseUrl;
    protected String environment;
    protected String accountId;
    private static boolean isReportInitialized = false;
    private long testStartTime;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logger.info("======================================================");
        logger.info("========= TEST SUITE STARTING =========");
        logger.info("======================================================");

        // Create directories for reports and screenshots
        createDirectory("test-output/screenshots");
        createDirectory("test-output/reports");
        createDirectory("test-output/logs");

        // Initialize ExtentReports
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String reportPath = "test-output/reports/TestReport_" + timestamp + ".html";
        ReportManager.initReport(reportPath);
        isReportInitialized = true;

        logger.info("Report initialized at: " + reportPath);
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "environment"})
    public void setUp(@Optional("chrome") String browser,
                      @Optional("qc") String env,
                      Method method) {
        testStartTime = System.currentTimeMillis();
        environment = env;
        logger.info("------------------------------------------------------");
        logger.info("Starting test: " + method.getName());
        logger.info("Browser: " + browser + " | Environment: " + environment);
        logger.info("------------------------------------------------------");

        // Load configuration
        ConfigReader.loadConfig(environment);
        baseUrl = ConfigReader.getProperty("base.url");
        accountId = ConfigReader.getProperty("account.id");

        // Initialize driver
        driver = DriverFactory.initializeDriver(browser);

        // Navigate to base URL
        logger.info("Navigating to base URL: " + baseUrl);
        driver.get(baseUrl);

        // Wait for page to load completely
        try {
            Thread.sleep(3000); // Hard wait for initial page load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Create test in report
        ReportManager.createTest(method.getName(),
                method.getDeclaringClass().getSimpleName() + " - " + method.getName());

        logger.info("Setup complete for test: " + method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        long duration = System.currentTimeMillis() - testStartTime;
        String testName = result.getMethod().getMethodName();

        logger.info("------------------------------------------------------");
        logger.info("Finishing test: " + testName);
        logger.info("Duration: " + duration + "ms");

        // Handle test result
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("TEST FAILED: " + testName);
            logger.error("Failure reason: " + result.getThrowable().getMessage());

            // Take screenshot on failure
            String screenshotPath = ScreenshotUtils.takeScreenshot(driver, testName);
            if (screenshotPath != null) {
                ReportManager.logFailWithScreenshot(
                        "Test failed: " + result.getThrowable().getMessage(),
                        screenshotPath
                );
                logger.info("Screenshot saved: " + screenshotPath);
            } else {
                ReportManager.logFail("Test failed: " + result.getThrowable().getMessage());
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("TEST PASSED: " + testName);
            ReportManager.logPass("Test passed successfully. Duration: " + duration + "ms");
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("TEST SKIPPED: " + testName);
            ReportManager.logSkip("Test skipped: " + result.getThrowable().getMessage());
        }

        // Quit driver
        if (driver != null) {
            try {
                DriverFactory.quitDriver();
            } catch (Exception e) {
                logger.error("Error during driver cleanup: " + e.getMessage());
            }
        }

        logger.info("Teardown complete for test: " + testName);
        logger.info("------------------------------------------------------");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        // Flush report
        if (isReportInitialized) {
            ReportManager.flushReport();
            logger.info("Report flushed successfully");
        }

        logger.info("======================================================");
        logger.info("========= TEST SUITE COMPLETED =========");
        logger.info("======================================================");
    }

    // Helper methods that add to the verbosity of the base class
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Directory created: " + path);
            } else {
                logger.warn("Failed to create directory: " + path);
            }
        }
    }

    protected void navigateToUrl(String url) {
        logger.info("Navigating to: " + url);
        driver.get(url);
        try {
            Thread.sleep(2000); // Wait for navigation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void refreshPage() {
        logger.info("Refreshing page...");
        driver.navigate().refresh();
        try {
            Thread.sleep(2000); // Wait after refresh
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }
}
