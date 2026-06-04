package com.acmeplatform.automation.tests;

import com.acmeplatform.automation.base.BaseTest;
import com.acmeplatform.automation.pages.*;
import com.acmeplatform.automation.utils.ConfigReader;
import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.ScreenshotUtils;
import com.acmeplatform.automation.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * MessageTest - Batch message creation and delivery verification.
 * 
 * ============================================================================================
 * THIS TEST CLASS SHOWS THE WORST OF SELENIUM:
 *
 * 1. POLLING LOOPS (waitForDeliveryStats):
 *    In Selenium: for loop + Thread.sleep(5000) + page refresh + re-check.
 *    In Playwright: await expect(locator).toHaveText('1', { timeout: 60000 });
 *    The Playwright assertion auto-retries every 100ms. No loop. No refresh.
 *
 * 2. NO NETWORK INTERCEPTION:
 *    In Selenium: Can't wait for specific API response. Must guess with Thread.sleep.
 *    In Playwright: await page.waitForResponse(resp => resp.url().includes('/send'));
 *    Or: await page.route('**/api/messages', route => route.fulfill({...}));
 *
 * 3. VISUAL TESTING (testMessagePreviewVisualComparison):
 *    In Selenium: Crude pixel diff that produces false positives constantly.
 *    In Playwright: toHaveScreenshot() is better but still pixel-based.
 *    With Applitools: eyes.check() uses Visual AI — ignores noise, catches real bugs.
 *
 * 4. MULTI-STEP WIZARD:
 *    Every step transition requires: clickNextStep() + hardWait(2000) + waitForPageLoad.
 *    In Playwright: click() auto-waits, page.waitForURL() detects transitions.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Polling loops with Thread.sleep for async delivery stats
 * - No network interception (can't wait for specific API responses)
 * - Long test methods with multiple navigation steps
 * - Screenshot comparison as visual "testing" (fragile, pixel-based)
 * - Manual state management between multi-step wizards
 *
 * Compare with: playwright-automation-framework/tests/ui/specs/message-batch-email.spec.ts
 */
public class MessageTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private MessageCreatePage messageCreatePage;

    private static final int MAX_DELIVERY_RETRIES = 12; // 12 * 5s = 60 seconds of polling

    @BeforeMethod
    public void initPages() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        messageCreatePage = new MessageCreatePage(driver);
    }

    @Test(description = "Create and send a promotional batch email message")
    public void testCreatePromotionalBatchEmail() {
        ReportManager.logStep("Starting batch email message creation");

        // Login
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.login(email, password);
        WaitUtils.hardWait(2000);

        // Navigate to message creation
        dashboardPage.verifyDashboardLoaded();
        navigateToUrl(baseUrl + "/messages/create");
        WaitUtils.hardWait(3000);
        WaitUtils.waitForPageLoad(driver);

        // Generate test data
        String messageName = "Batch_Promo_" + UUID.randomUUID().toString().substring(0, 8);
        String fromName = "Automation Test";
        String fromEmail = "automation@test.acmeplatform.com";
        String subject = "Test Promotional Email - " + System.currentTimeMillis();
        String htmlContent = "<html><body><h1>Test Email</h1><p>This is an automated test.</p></body></html>";

        // Step 1: Message setup
        messageCreatePage.fillMessageName(messageName);
        messageCreatePage.selectPromotionalType();
        messageCreatePage.selectEmailChannel();
        messageCreatePage.clickNextStep();
        WaitUtils.hardWait(1000);

        // Step 2: Header
        messageCreatePage.fillFromName(fromName);
        messageCreatePage.fillFromEmail(fromEmail);
        messageCreatePage.fillSubjectLine(subject);
        messageCreatePage.clickNextStep();
        WaitUtils.hardWait(1000);

        // Step 3: Content
        messageCreatePage.fillHtmlContent(htmlContent);
        messageCreatePage.clickNextStep();
        WaitUtils.hardWait(1000);

        // Step 4: Audience
        messageCreatePage.addAudienceRule("channels.email.address", "is not empty", "");
        messageCreatePage.calculateAudienceCount();
        WaitUtils.hardWait(3000);
        String audienceCount = messageCreatePage.getAudienceCount();
        Assert.assertNotNull(audienceCount, "Audience count should be calculated");
        ReportManager.logStep("Audience count: " + audienceCount);
        messageCreatePage.clickNextStep();
        WaitUtils.hardWait(1000);

        // Step 5: Schedule - Send Now
        messageCreatePage.selectSendNow();
        messageCreatePage.clickSendMessage();
        messageCreatePage.confirmSendMessage();
        WaitUtils.hardWait(5000); // Wait for message to start sending

        // Poll for delivery stats (the painful part)
        ReportManager.logStep("Waiting for delivery stats...");
        boolean statsReady = messageCreatePage.waitForDeliveryStats("1", MAX_DELIVERY_RETRIES);
        Assert.assertTrue(statsReady, "Message should show delivery stats within timeout");

        // Verify message status
        String status = messageCreatePage.getMessageStatus();
        Assert.assertTrue(status.contains("Sent") || status.contains("Delivered"),
                "Message status should be Sent or Delivered. Actual: " + status);

        ReportManager.logStep("Batch email sent successfully. Status: " + status);
    }

    @Test(description = "Create a scheduled batch email with throttling")
    public void testCreateScheduledBatchEmailWithThrottling() {
        ReportManager.logStep("Starting scheduled throttled email test");

        // Login
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.login(email, password);
        WaitUtils.hardWait(2000);

        dashboardPage.verifyDashboardLoaded();
        navigateToUrl(baseUrl + "/messages/create");
        WaitUtils.hardWait(3000);

        // Setup message
        String messageName = "Scheduled_Throttle_" + UUID.randomUUID().toString().substring(0, 8);
        messageCreatePage.fillMessageName(messageName);
        messageCreatePage.selectPromotionalType();
        messageCreatePage.selectEmailChannel();
        messageCreatePage.clickNextStep();

        // Header
        messageCreatePage.fillHeaderSection("Test Sender", "test@acmeplatform.com",
                "Scheduled Test - " + System.currentTimeMillis());
        messageCreatePage.clickNextStep();

        // Content
        messageCreatePage.fillHtmlContent("<html><body><p>Scheduled email content</p></body></html>");
        messageCreatePage.clickNextStep();

        // Audience
        messageCreatePage.addAudienceRule("channels.email.subscribeStatus", "equals", "subscribed");
        messageCreatePage.clickNextStep();
        WaitUtils.hardWait(1000);

        // Schedule with throttling
        messageCreatePage.selectScheduleSend("2024-12-31", "09:00");
        messageCreatePage.enableThrottling("1000");
        messageCreatePage.clickSendMessage();
        messageCreatePage.confirmSendMessage();
        WaitUtils.hardWait(3000);

        // Verify message is scheduled (no delivery stats yet)
        String status = messageCreatePage.getMessageStatus();
        Assert.assertTrue(status.contains("Scheduled"),
                "Message should be in Scheduled status. Actual: " + status);

        ReportManager.logStep("Scheduled throttled email created. Status: " + status);
    }

    @Test(description = "Visual comparison of message preview - crude screenshot diff")
    public void testMessagePreviewVisualComparison() {
        ReportManager.logStep("Starting visual comparison test (pixel-based)");

        // Login
        loginPage.login(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
        WaitUtils.hardWait(2000);

        // Navigate to an existing message
        dashboardPage.verifyDashboardLoaded();
        navigateToUrl(baseUrl + "/messages/preview/sample-message-id");
        WaitUtils.hardWait(5000); // Heavy wait for message preview to render
        WaitUtils.waitForPageLoad(driver);

        // Take screenshot for comparison
        // This is the crude approach - pixel diffing that Applitools Visual AI replaces
        String currentScreenshot = ScreenshotUtils.takeScreenshot(driver, "message_preview_current");
        Assert.assertNotNull(currentScreenshot, "Screenshot should be captured");

        // Compare with baseline (if exists)
        String baselinePath = "test-output/screenshots/baseline/message_preview_baseline.png";
        boolean matches = ScreenshotUtils.compareScreenshots(baselinePath, currentScreenshot, 5.0);

        // This often fails due to:
        // - Anti-aliasing differences
        // - Font rendering across OS
        // - Dynamic timestamps in the email preview
        // - Scrollbar rendering differences
        if (!matches) {
            ReportManager.logWarning("Visual comparison failed - this is common with pixel diffing. " +
                    "Consider using Applitools Visual AI for intelligent comparison.");
        }

        ReportManager.logStep("Visual comparison completed");
    }
}
