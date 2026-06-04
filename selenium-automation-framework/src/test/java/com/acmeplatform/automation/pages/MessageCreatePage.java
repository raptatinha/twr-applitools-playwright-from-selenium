package com.acmeplatform.automation.pages;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * MessageCreatePage - Handles the multi-step message creation flow.
 * 
 * Pain points demonstrated:
 * - ~400 lines for a complex multi-step flow
 * - Duplicated navigation logic between steps
 * - Heavy use of Thread.sleep for async operations
 * - Manual polling for message delivery stats
 * - No network interception - must wait arbitrary times for API responses
 */
public class MessageCreatePage extends BasePage {

    // ==================== Step Navigation ====================
    private static final By STEP_HEADER = By.cssSelector("[data-auto-qa='message-step-header']");
    private static final By NEXT_STEP_BUTTON = By.cssSelector("[data-auto-qa='next-step-button']");
    private static final By PREVIOUS_STEP_BUTTON = By.cssSelector("[data-auto-qa='previous-step-button']");
    private static final By SAVE_DRAFT_BUTTON = By.cssSelector("[data-auto-qa='save-draft-button']");

    // ==================== Step 1: Message Type ====================
    private static final By MESSAGE_NAME_INPUT = By.cssSelector("[data-auto-qa='message-name-input']");
    private static final By MESSAGE_TYPE_PROMOTIONAL = By.cssSelector("[data-auto-qa='type-promotional']");
    private static final By MESSAGE_TYPE_TRANSACTIONAL = By.cssSelector("[data-auto-qa='type-transactional']");
    private static final By CHANNEL_EMAIL = By.cssSelector("[data-auto-qa='channel-email']");
    private static final By CHANNEL_SMS = By.cssSelector("[data-auto-qa='channel-sms']");

    // ==================== Step 2: Header ====================
    private static final By FROM_NAME_INPUT = By.cssSelector("[data-auto-qa='from-name-input']");
    private static final By FROM_EMAIL_INPUT = By.cssSelector("[data-auto-qa='from-email-input']");
    private static final By REPLY_TO_INPUT = By.cssSelector("[data-auto-qa='reply-to-input']");
    private static final By SUBJECT_LINE_INPUT = By.cssSelector("[data-auto-qa='subject-line-input']");
    private static final By PREHEADER_INPUT = By.cssSelector("[data-auto-qa='preheader-input']");

    // ==================== Step 3: Content ====================
    private static final By CONTENT_EDITOR = By.cssSelector("[data-auto-qa='content-editor']");
    private static final By HTML_EDITOR_TAB = By.cssSelector("[data-auto-qa='html-editor-tab']");
    private static final By VISUAL_EDITOR_TAB = By.cssSelector("[data-auto-qa='visual-editor-tab']");
    private static final By HTML_CODE_INPUT = By.cssSelector("[data-auto-qa='html-code-input'] textarea");
    private static final By PREVIEW_BUTTON = By.cssSelector("[data-auto-qa='preview-button']");

    // ==================== Step 4: Audience ====================
    private static final By AUDIENCE_SECTION = By.cssSelector("[data-auto-qa='audience-section']");
    private static final By ADD_RULE_BUTTON = By.cssSelector("[data-auto-qa='add-rule-button']");
    private static final By RULE_FIELD_DROPDOWN = By.cssSelector("[data-auto-qa='rule-field-select']");
    private static final By RULE_OPERATOR_DROPDOWN = By.cssSelector("[data-auto-qa='rule-operator-select']");
    private static final By RULE_VALUE_INPUT = By.cssSelector("[data-auto-qa='rule-value-input']");
    private static final By AUDIENCE_COUNT = By.cssSelector("[data-auto-qa='audience-count']");
    private static final By CALCULATE_AUDIENCE_BUTTON = By.cssSelector("[data-auto-qa='calculate-audience-btn']");

    // ==================== Step 5: Schedule ====================
    private static final By SEND_NOW_OPTION = By.cssSelector("[data-auto-qa='send-now']");
    private static final By SCHEDULE_OPTION = By.cssSelector("[data-auto-qa='schedule-send']");
    private static final By SCHEDULE_DATE_INPUT = By.cssSelector("[data-auto-qa='schedule-date']");
    private static final By SCHEDULE_TIME_INPUT = By.cssSelector("[data-auto-qa='schedule-time']");
    private static final By THROTTLE_TOGGLE = By.cssSelector("[data-auto-qa='throttle-toggle']");
    private static final By THROTTLE_DAILY_LIMIT = By.cssSelector("[data-auto-qa='throttle-daily-limit']");
    private static final By SEND_MESSAGE_BUTTON = By.cssSelector("[data-auto-qa='send-message-button']");
    private static final By CONFIRM_SEND_BUTTON = By.cssSelector("[data-auto-qa='confirm-send-button']");

    // ==================== Performance / Stats ====================
    private static final By PERFORMANCE_TAB = By.cssSelector("[data-auto-qa='performance-tab']");
    private static final By TOTAL_SENT_STAT = By.cssSelector("[data-auto-qa='stat-total-sent']");
    private static final By DELIVERED_STAT = By.cssSelector("[data-auto-qa='stat-delivered']");
    private static final By OPENED_STAT = By.cssSelector("[data-auto-qa='stat-opened']");
    private static final By CLICKED_STAT = By.cssSelector("[data-auto-qa='stat-clicked']");
    private static final By MESSAGE_STATUS = By.cssSelector("[data-auto-qa='message-status']");

    public MessageCreatePage(WebDriver driver) {
        super(driver);
    }

    // ==================== Step Navigation (duplicated wait patterns) ====================

    public void clickNextStep() {
        logger.info("Clicking next step");
        scrollToTop();
        WaitUtils.hardWait(500);
        WaitUtils.waitForElementClickable(driver, NEXT_STEP_BUTTON);
        WaitUtils.hardWait(300);
        click(NEXT_STEP_BUTTON);
        WaitUtils.hardWait(2000); // Wait for step transition animation
        WaitUtils.waitForPageLoad(driver);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Moved to next step");
    }

    public void clickPreviousStep() {
        scrollToTop();
        WaitUtils.hardWait(500);
        WaitUtils.waitForElementClickable(driver, PREVIOUS_STEP_BUTTON);
        WaitUtils.hardWait(300);
        click(PREVIOUS_STEP_BUTTON);
        WaitUtils.hardWait(2000);
        WaitUtils.waitForPageLoad(driver);
        waitForLoadingSpinnerToDisappear();
    }

    // ==================== Step 1: Message Setup ====================

    public void fillMessageName(String name) {
        logger.info("Filling message name: " + name);
        WaitUtils.waitForElementVisible(driver, MESSAGE_NAME_INPUT);
        WaitUtils.hardWait(500);
        type(MESSAGE_NAME_INPUT, name);
        WaitUtils.hardWait(300);
        ReportManager.logStep("Filled message name: " + name);
    }

    public void selectPromotionalType() {
        WaitUtils.waitForElementClickable(driver, MESSAGE_TYPE_PROMOTIONAL);
        WaitUtils.hardWait(300);
        click(MESSAGE_TYPE_PROMOTIONAL);
        WaitUtils.hardWait(500);
        ReportManager.logStep("Selected promotional message type");
    }

    public void selectTransactionalType() {
        WaitUtils.waitForElementClickable(driver, MESSAGE_TYPE_TRANSACTIONAL);
        WaitUtils.hardWait(300);
        click(MESSAGE_TYPE_TRANSACTIONAL);
        WaitUtils.hardWait(500);
        ReportManager.logStep("Selected transactional message type");
    }

    public void selectEmailChannel() {
        WaitUtils.waitForElementClickable(driver, CHANNEL_EMAIL);
        WaitUtils.hardWait(300);
        click(CHANNEL_EMAIL);
        WaitUtils.hardWait(500);
    }

    public void selectSmsChannel() {
        WaitUtils.waitForElementClickable(driver, CHANNEL_SMS);
        WaitUtils.hardWait(300);
        click(CHANNEL_SMS);
        WaitUtils.hardWait(500);
    }

    // ==================== Step 2: Header ====================

    public void fillFromName(String name) {
        logger.info("Filling from name: " + name);
        WaitUtils.waitForElementVisible(driver, FROM_NAME_INPUT);
        WaitUtils.hardWait(500);
        type(FROM_NAME_INPUT, name);
        WaitUtils.hardWait(300);
    }

    public void fillFromEmail(String email) {
        WaitUtils.waitForElementVisible(driver, FROM_EMAIL_INPUT);
        WaitUtils.hardWait(500);
        type(FROM_EMAIL_INPUT, email);
        WaitUtils.hardWait(300);
    }

    public void fillReplyTo(String email) {
        WaitUtils.waitForElementVisible(driver, REPLY_TO_INPUT);
        WaitUtils.hardWait(500);
        type(REPLY_TO_INPUT, email);
        WaitUtils.hardWait(300);
    }

    public void fillSubjectLine(String subject) {
        logger.info("Filling subject: " + subject);
        WaitUtils.waitForElementVisible(driver, SUBJECT_LINE_INPUT);
        WaitUtils.hardWait(500);
        type(SUBJECT_LINE_INPUT, subject);
        WaitUtils.hardWait(300);
        ReportManager.logStep("Filled subject: " + subject);
    }

    public void fillPreheader(String preheader) {
        WaitUtils.waitForElementVisible(driver, PREHEADER_INPUT);
        WaitUtils.hardWait(500);
        type(PREHEADER_INPUT, preheader);
        WaitUtils.hardWait(300);
    }

    public void fillHeaderSection(String fromName, String fromEmail, String subject) {
        fillFromName(fromName);
        fillFromEmail(fromEmail);
        fillSubjectLine(subject);
        ReportManager.logStep("Header section completed");
    }

    // ==================== Step 3: Content ====================

    public void switchToHtmlEditor() {
        WaitUtils.waitForElementClickable(driver, HTML_EDITOR_TAB);
        WaitUtils.hardWait(300);
        click(HTML_EDITOR_TAB);
        WaitUtils.hardWait(1000); // Wait for editor to switch
    }

    public void fillHtmlContent(String htmlContent) {
        logger.info("Filling HTML content");
        switchToHtmlEditor();
        WaitUtils.waitForElementVisible(driver, HTML_CODE_INPUT);
        WaitUtils.hardWait(500);
        WebElement editor = driver.findElement(HTML_CODE_INPUT);
        editor.clear();
        WaitUtils.hardWait(300);
        editor.sendKeys(htmlContent);
        WaitUtils.hardWait(1000); // Wait for content to render
        ReportManager.logStep("HTML content filled");
    }

    // ==================== Step 4: Audience ====================

    public void addAudienceRule(String field, String operator, String value) {
        logger.info("Adding audience rule: " + field + " " + operator + " " + value);
        WaitUtils.waitForElementVisible(driver, AUDIENCE_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_RULE_BUTTON);
        WaitUtils.hardWait(1000);

        selectDropdownByText(RULE_FIELD_DROPDOWN, field);
        WaitUtils.hardWait(500);

        selectDropdownByText(RULE_OPERATOR_DROPDOWN, operator);
        WaitUtils.hardWait(500);

        type(RULE_VALUE_INPUT, value);
        WaitUtils.hardWait(500);

        ReportManager.logStep("Added audience rule: " + field + " " + operator + " " + value);
    }

    public void calculateAudienceCount() {
        click(CALCULATE_AUDIENCE_BUTTON);
        WaitUtils.hardWait(3000); // Wait for audience calculation API
        waitForLoadingSpinnerToDisappear();
    }

    public String getAudienceCount() {
        WaitUtils.waitForElementVisible(driver, AUDIENCE_COUNT, 30);
        WaitUtils.hardWait(500);
        return driver.findElement(AUDIENCE_COUNT).getText();
    }

    // ==================== Step 5: Schedule & Send ====================

    public void selectSendNow() {
        WaitUtils.waitForElementClickable(driver, SEND_NOW_OPTION);
        WaitUtils.hardWait(300);
        click(SEND_NOW_OPTION);
        WaitUtils.hardWait(500);
        ReportManager.logStep("Selected Send Now");
    }

    public void selectScheduleSend(String date, String time) {
        WaitUtils.waitForElementClickable(driver, SCHEDULE_OPTION);
        WaitUtils.hardWait(300);
        click(SCHEDULE_OPTION);
        WaitUtils.hardWait(1000);

        type(SCHEDULE_DATE_INPUT, date);
        WaitUtils.hardWait(300);
        type(SCHEDULE_TIME_INPUT, time);
        WaitUtils.hardWait(300);
        ReportManager.logStep("Scheduled for: " + date + " at " + time);
    }

    public void enableThrottling(String dailyLimit) {
        WaitUtils.waitForElementClickable(driver, THROTTLE_TOGGLE);
        WaitUtils.hardWait(300);
        click(THROTTLE_TOGGLE);
        WaitUtils.hardWait(1000);

        WaitUtils.waitForElementVisible(driver, THROTTLE_DAILY_LIMIT);
        type(THROTTLE_DAILY_LIMIT, dailyLimit);
        WaitUtils.hardWait(300);
        ReportManager.logStep("Throttling enabled: " + dailyLimit + "/day");
    }

    public void clickSendMessage() {
        logger.info("Clicking send message");
        scrollToElement(SEND_MESSAGE_BUTTON);
        WaitUtils.waitForElementClickable(driver, SEND_MESSAGE_BUTTON);
        WaitUtils.hardWait(500);
        click(SEND_MESSAGE_BUTTON);
        WaitUtils.hardWait(2000); // Wait for confirmation modal
        ReportManager.logStep("Clicked send message");
    }

    public void confirmSendMessage() {
        logger.info("Confirming message send");
        WaitUtils.waitForElementVisible(driver, CONFIRM_SEND_BUTTON);
        WaitUtils.hardWait(500);
        click(CONFIRM_SEND_BUTTON);
        WaitUtils.hardWait(3000); // Wait for send API call
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Message send confirmed");
    }

    // ==================== Performance Stats (Polling) ====================

    /**
     * Waits for message delivery stats to appear.
     * This demonstrates the painful polling pattern - no network interception,
     * no auto-retrying assertions, just raw loops with Thread.sleep.
     */
    public boolean waitForDeliveryStats(String expectedSentCount, int maxRetries) {
        logger.info("Waiting for delivery stats. Expected sent: " + expectedSentCount);

        // First navigate to performance tab
        WaitUtils.waitForElementClickable(driver, PERFORMANCE_TAB);
        WaitUtils.hardWait(500);
        click(PERFORMANCE_TAB);
        WaitUtils.hardWait(2000);
        waitForLoadingSpinnerToDisappear();

        // Poll for stats with page refresh
        for (int i = 0; i < maxRetries; i++) {
            try {
                WaitUtils.waitForElementVisible(driver, TOTAL_SENT_STAT, 10);
                String sentCount = driver.findElement(TOTAL_SENT_STAT).getText();
                logger.info("Attempt " + (i + 1) + ": Total Sent = " + sentCount);

                if (sentCount.contains(expectedSentCount)) {
                    ReportManager.logStep("Delivery stats confirmed: " + sentCount + " sent");
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Stats not ready yet, attempt " + (i + 1));
            }

            // Wait and refresh to check again
            WaitUtils.hardWait(5000); // 5 seconds between polls
            refreshPage();
            WaitUtils.hardWait(2000);
            waitForLoadingSpinnerToDisappear();

            // Re-click performance tab after refresh
            try {
                WaitUtils.waitForElementClickable(driver, PERFORMANCE_TAB);
                click(PERFORMANCE_TAB);
                WaitUtils.hardWait(1500);
            } catch (Exception e) {
                // Tab may already be selected
            }
        }

        logger.error("Delivery stats not reached expected count after " + maxRetries + " retries");
        return false;
    }

    public String getMessageStatus() {
        WaitUtils.waitForElementVisible(driver, MESSAGE_STATUS);
        WaitUtils.hardWait(500);
        return driver.findElement(MESSAGE_STATUS).getText();
    }

    // ==================== Complete Flow Methods ====================

    public void createBatchEmailMessage(String name, String fromName, String fromEmail,
                                         String subject, String htmlContent) {
        // Step 1: Setup
        fillMessageName(name);
        selectPromotionalType();
        selectEmailChannel();
        clickNextStep();

        // Step 2: Header
        fillHeaderSection(fromName, fromEmail, subject);
        clickNextStep();

        // Step 3: Content
        fillHtmlContent(htmlContent);
        clickNextStep();
    }
}
