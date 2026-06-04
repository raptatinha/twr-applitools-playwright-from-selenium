package com.acmeplatform.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ReportManager - Manages ExtentReports lifecycle.
 * 
 * ============================================================================================
 * THIS ENTIRE 120-LINE FILE IS UNNECESSARY IN PLAYWRIGHT.
 *
 * Playwright built-in reporters:
 *   // playwright.config.ts
 *   reporter: [['html'], ['junit', { outputFile: 'results.xml' }]]
 *
 * That's it. One line. You get:
 *   - Beautiful HTML report with screenshots, traces, and video
 *   - JUnit XML for CI integration
 *   - Filterable by status, browser, project
 *   - Timeline view showing parallel execution
 *   - Direct links to Trace Viewer for failed tests
 *   - Slack reporter via npm package (no custom code)
 *
 * What you have to build MANUALLY with Selenium:
 *   - ExtentReports initialization (10 lines)
 *   - Reporter configuration (10 lines)
 *   - System info collection (6 lines)
 *   - Test creation per method (ThreadLocal management)
 *   - Pass/Fail/Skip logging (6 separate methods)
 *   - Screenshot attachment to report (try/catch with path management)
 *   - Report flushing in @AfterSuite (easy to forget = no report)
 *   - Thread safety with ThreadLocal (parallel execution bugs)
 *
 * TOTAL: ~120 lines of code that does LESS than Playwright's built-in reporter.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - 100+ lines just to set up reporting that Playwright gives for free
 * - Manual screenshot embedding into reports
 * - Thread-local management for parallel execution
 * - Must be initialized in @BeforeSuite and flushed in @AfterSuite
 * - Separate from the actual test runner (not integrated)
 * - Every test status must be manually logged
 */
public class ReportManager {

    private static final Logger logger = LogManager.getLogger(ReportManager.class);
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
    private static String reportPath;

    public static void initReport(String path) {
        reportPath = path;
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(path);

        // Configure reporter appearance
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Acme Automation Test Report");
        sparkReporter.config().setReportName("Selenium Test Execution Report");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        sparkReporter.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);

        // System info
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", System.getProperty("browser", "chrome"));
        extent.setSystemInfo("Environment", System.getProperty("environment", "qc"));
        extent.setSystemInfo("Tester", System.getProperty("user.name"));
        extent.setSystemInfo("Execution Date",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        logger.info("ExtentReports initialized with path: " + path);
    }

    public static void createTest(String testName, String description) {
        ExtentTest test = extent.createTest(testName, description);
        testThread.set(test);
        logger.debug("Test created in report: " + testName);
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    public static void logPass(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.PASS, message);
        }
    }

    public static void logFail(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.FAIL, message);
        }
    }

    public static void logFailWithScreenshot(String message, String screenshotPath) {
        ExtentTest test = getTest();
        if (test != null) {
            try {
                test.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                test.log(Status.FAIL, message + " [Screenshot failed to attach]");
                logger.error("Failed to attach screenshot to report: " + e.getMessage());
            }
        }
    }

    public static void logInfo(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.INFO, message);
        }
    }

    public static void logSkip(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.SKIP, message);
        }
    }

    public static void logWarning(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.WARNING, message);
        }
    }

    public static void logStep(String stepDescription) {
        ExtentTest test = getTest();
        if (test != null) {
            test.info("Step: " + stepDescription);
        }
        logger.info("Step: " + stepDescription);
    }

    public static void logScreenshot(String screenshotPath, String title) {
        ExtentTest test = getTest();
        if (test != null) {
            try {
                test.info(title, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                logger.error("Failed to log screenshot: " + e.getMessage());
            }
        }
    }

    public static void flushReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Report flushed to: " + reportPath);
        }
    }

    public static String getReportPath() {
        return reportPath;
    }
}
