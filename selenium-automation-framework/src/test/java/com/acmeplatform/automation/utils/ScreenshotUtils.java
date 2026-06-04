package com.acmeplatform.automation.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotUtils - Manual screenshot capture and comparison.
 * 
 * ============================================================================================
 * IN PLAYWRIGHT:
 *   Screenshots: screenshot: 'only-on-failure' in config. Done.
 *   Trace (better than screenshots): trace: 'retain-on-failure'. Full DOM + network + console.
 *   Visual comparison: await expect(page).toHaveScreenshot('name.png');
 *
 * THIS FILE implements:
 *   - Manual screenshot capture (takeScreenshot) → Playwright: automatic
 *   - File naming with timestamps → Playwright: automatic
 *   - Directory creation → Playwright: automatic
 *   - Base64 encoding for reports → Playwright: automatic
 *   - Baseline management → Playwright: npx playwright test --update-snapshots
 *   - PIXEL COMPARISON (compareScreenshots) → This is where Applitools comes in.
 *
 * THE PIXEL COMPARISON PROBLEM:
 *   The compareScreenshots() method below does pixel-by-pixel comparison.
 *   This ALWAYS fails in real projects because of:
 *   - Anti-aliasing differences between OS/browsers
 *   - Font rendering (sub-pixel rendering varies)
 *   - Dynamic content (timestamps, counters, avatars)
 *   - Scrollbar rendering
 *   - Animation timing
 *   - Screen resolution / DPI differences
 *
 *   Playwright's toHaveScreenshot() is better (threshold-based) but still pixel-level.
 *   Only Applitools Visual AI can understand VISUAL INTENT and ignore noise.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Must manually implement screenshot-on-failure
 * - File management (naming, directories, cleanup) is your problem
 * - No built-in trace or video recording
 * - Pixel-based comparison is fragile and produces false positives
 * - No integration with test runner or reporting (must wire manually)
 */
public class ScreenshotUtils {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";
    private static final String BASELINE_DIR = "test-output/screenshots/baseline/";
    private static final String DIFF_DIR = "test-output/screenshots/diff/";

    public static String takeScreenshot(WebDriver driver, String testName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = testName + "_" + timestamp + ".png";
            String destination = SCREENSHOT_DIR + fileName;

            File destFile = new File(destination);
            FileUtils.copyFile(source, destFile);

            logger.info("Screenshot taken: " + destination);
            return destination;
        } catch (IOException e) {
            logger.error("Failed to take screenshot: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Screenshot error: " + e.getMessage());
            return null;
        }
    }

    public static String takeScreenshotAsBase64(WebDriver driver) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            return ts.getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Failed to take base64 screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crude pixel-by-pixel comparison - demonstrates why screenshot testing
     * in Selenium is painful and why Visual AI (Applitools) is needed.
     * 
     * Problems with this approach:
     * - Anti-aliasing differences cause false positives
     * - Font rendering varies across OS/browsers
     * - Dynamic content (timestamps, ads) breaks every run
     * - No intelligent diffing - every pixel mismatch is a "failure"
     * - Baseline management is manual
     */
    public static boolean compareScreenshots(String baselinePath, String actualPath, double threshold) {
        try {
            BufferedImage baselineImg = ImageIO.read(new File(baselinePath));
            BufferedImage actualImg = ImageIO.read(new File(actualPath));

            if (baselineImg.getWidth() != actualImg.getWidth() ||
                    baselineImg.getHeight() != actualImg.getHeight()) {
                logger.error("Screenshot dimensions don't match!");
                return false;
            }

            int width = baselineImg.getWidth();
            int height = baselineImg.getHeight();
            long totalPixels = (long) width * height;
            long diffPixels = 0;

            // Create diff image
            BufferedImage diffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int baselinePixel = baselineImg.getRGB(x, y);
                    int actualPixel = actualImg.getRGB(x, y);

                    if (baselinePixel != actualPixel) {
                        diffPixels++;
                        diffImg.setRGB(x, y, 0xFF0000); // Mark diff in red
                    } else {
                        diffImg.setRGB(x, y, actualPixel);
                    }
                }
            }

            double diffPercentage = (double) diffPixels / totalPixels * 100;
            logger.info("Screenshot comparison - Diff: " + String.format("%.2f", diffPercentage) + "%");

            // Save diff image
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String diffPath = DIFF_DIR + "diff_" + timestamp + ".png";
            new File(DIFF_DIR).mkdirs();
            ImageIO.write(diffImg, "png", new File(diffPath));

            return diffPercentage <= threshold;
        } catch (IOException e) {
            logger.error("Failed to compare screenshots: " + e.getMessage());
            return false;
        }
    }

    public static void saveBaseline(WebDriver driver, String pageName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);

            String fileName = pageName + "_baseline.png";
            String destination = BASELINE_DIR + fileName;

            new File(BASELINE_DIR).mkdirs();
            FileUtils.copyFile(source, new File(destination));

            logger.info("Baseline saved: " + destination);
        } catch (IOException e) {
            logger.error("Failed to save baseline: " + e.getMessage());
        }
    }

    public static void cleanupOldScreenshots(int daysToKeep) {
        File screenshotDir = new File(SCREENSHOT_DIR);
        if (screenshotDir.exists()) {
            long cutoffTime = System.currentTimeMillis() - ((long) daysToKeep * 24 * 60 * 60 * 1000);
            File[] files = screenshotDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            logger.debug("Deleted old screenshot: " + file.getName());
                        }
                    }
                }
            }
        }
    }
}
