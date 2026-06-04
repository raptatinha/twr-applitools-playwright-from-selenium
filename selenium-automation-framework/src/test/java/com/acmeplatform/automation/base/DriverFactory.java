package com.acmeplatform.automation.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DriverFactory - Manages WebDriver instance creation and configuration.
 * 
 * ============================================================================================
 * THIS ENTIRE CLASS IS UNNECESSARY IN PLAYWRIGHT.
 *
 * Playwright handles browser lifecycle through its config file:
 *   // playwright.config.ts
 *   projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }]
 *
 * That's ONE line vs 140 lines of Java.
 *
 * What DriverFactory does manually:
 *   - Browser binary management → Playwright installs browsers via `npx playwright install`
 *   - Browser options (headless, window size) → playwright.config.ts: use: { headless: true }
 *   - Parallel execution (ThreadLocal) → Playwright runs workers in parallel by default
 *   - Timeout configuration → playwright.config.ts: timeout: 30000
 *   - Browser cleanup (quit/dispose) → Automatic. Browser context closes after each test.
 *
 * PROBLEMS WITH THIS PATTERN:
 *   - if/else chains for every browser type (Chrome/Firefox/Edge)
 *   - ThreadLocal for parallel execution (error-prone, memory leaks)
 *   - Remote WebDriver setup adds another 30 lines of config
 *   - WebDriverManager versioning issues (driver ↔ browser mismatch)
 *   - Implicit wait conflicts with explicit waits (classic Selenium trap)
 *   - Every new browser option requires code changes and recompilation
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Verbose if/else chains for browser selection
 * - Manual driver binary management
 * - No built-in parallelism support (ThreadLocal workaround)
 * - Implicit wait as primary wait strategy
 * - Hardcoded timeout values scattered throughout
 */
public class DriverFactory {

    private static final Logger logger = LogManager.getLogger(DriverFactory.class);
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final int IMPLICIT_WAIT_SECONDS = 10;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;
    private static final int SCRIPT_TIMEOUT_SECONDS = 30;

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static WebDriver initializeDriver(String browserName) {
        WebDriver driver = null;
        String browser = browserName != null ? browserName : System.getProperty("browser", "chrome");
        String environment = System.getProperty("environment", "qc");
        String headless = System.getProperty("headless", "false");
        String remoteUrl = System.getProperty("remote.url", "");

        logger.info("========= Initializing browser: " + browser + " =========");
        logger.info("========= Environment: " + environment + " =========");
        logger.info("========= Headless: " + headless + " =========");

        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            driver = initializeRemoteDriver(browser, remoteUrl, headless);
        } else {
            if (browser.equalsIgnoreCase("chrome")) {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--disable-extensions");
                options.addArguments("--disable-popup-blocking");
                options.addArguments("--disable-notifications");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                if (headless.equalsIgnoreCase("true")) {
                    options.addArguments("--headless=new");
                }
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("credentials_enable_service", false);
                prefs.put("profile.password_manager_enabled", false);
                prefs.put("download.default_directory", System.getProperty("user.dir") + "/downloads");
                options.setExperimentalOption("prefs", prefs);
                driver = new ChromeDriver(options);
                logger.info("Chrome browser initialized successfully");
            } else if (browser.equalsIgnoreCase("firefox")) {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--width=1920");
                options.addArguments("--height=1080");
                if (headless.equalsIgnoreCase("true")) {
                    options.addArguments("--headless");
                }
                driver = new FirefoxDriver(options);
                logger.info("Firefox browser initialized successfully");
            } else if (browser.equalsIgnoreCase("edge")) {
                WebDriverManager.edgedriver().setup();
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--window-size=1920,1080");
                if (headless.equalsIgnoreCase("true")) {
                    options.addArguments("--headless=new");
                }
                driver = new EdgeDriver(options);
                logger.info("Edge browser initialized successfully");
            } else {
                logger.error("Browser not supported: " + browser);
                throw new RuntimeException("Browser not supported: " + browser + ". Supported browsers: chrome, firefox, edge");
            }
        }

        // Set timeouts - these are applied globally and can conflict with explicit waits
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(SCRIPT_TIMEOUT_SECONDS));
        driver.manage().window().maximize();

        // Delete all cookies to start fresh
        driver.manage().deleteAllCookies();

        driverThreadLocal.set(driver);
        logger.info("Driver initialized and stored in ThreadLocal");

        return driver;
    }

    private static WebDriver initializeRemoteDriver(String browser, String remoteUrl, String headless) {
        WebDriver driver = null;
        DesiredCapabilities capabilities = new DesiredCapabilities();

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            if (headless.equalsIgnoreCase("true")) {
                options.addArguments("--headless=new");
            }
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            if (headless.equalsIgnoreCase("true")) {
                options.addArguments("--headless");
            }
            capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        }

        try {
            driver = new RemoteWebDriver(new URL(remoteUrl), capabilities);
            logger.info("Remote WebDriver initialized at: " + remoteUrl);
        } catch (MalformedURLException e) {
            logger.error("Invalid remote URL: " + remoteUrl);
            throw new RuntimeException("Failed to initialize remote driver: " + e.getMessage());
        }

        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("Browser closed successfully");
            } catch (Exception e) {
                logger.error("Error while closing browser: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
