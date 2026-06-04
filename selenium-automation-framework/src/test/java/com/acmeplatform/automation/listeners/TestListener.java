package com.acmeplatform.automation.listeners;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.ScreenshotUtils;
import com.acmeplatform.automation.base.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestListener - TestNG listener for custom reporting hooks.
 * 
 * Pain point: You need a separate listener class just to hook into test lifecycle.
 * In Playwright, reporters are configured in playwright.config.ts with zero boilerplate.
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("Test Suite started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Test Suite finished: " + context.getName());
        logger.info("Passed: " + context.getPassedTests().size());
        logger.info("Failed: " + context.getFailedTests().size());
        logger.info("Skipped: " + context.getSkippedTests().size());
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: " + result.getMethod().getMethodName());
        logger.error("Reason: " + result.getThrowable().getMessage());

        // Take screenshot on failure (manual wiring required)
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            String screenshot = ScreenshotUtils.takeScreenshot(driver, result.getMethod().getMethodName());
            if (screenshot != null) {
                ReportManager.logFailWithScreenshot("Test failed", screenshot);
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not used
    }
}
