package com.acmeplatform.automation.pages;

import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * ContactPage - Handles ALL contact-related operations: creation, editing, profile viewing,
 * attribute management, channel configuration, list assignment, and deletion.
 *
 * ============================================================================================
 * WARNING: This file has grown over time as features were added. It handles too many
 * responsibilities (create, read, update, delete) in a single class. This is a common pattern
 * in Selenium projects where page objects accumulate methods without refactoring.
 *
 * KNOWN ISSUES:
 * - File is 500+ lines and growing
 * - Duplicated wait patterns in every single method (see addStringAttribute vs addNumberAttribute
 *   vs addDateAttribute - they're all copy-paste with one word changed)
 * - Mix of creation and verification logic in the same class
 * - Locators section alone is 70+ lines
 * - Thread.sleep / hardWait used after EVERY action because Selenium doesn't auto-wait
 * - No way to share this page object across tests without instantiating manually
 *
 * IN PLAYWRIGHT: This would be split into thin, focused page objects:
 * - ContactCreatePage (just creation form)
 * - ContactProfilePage (just viewing)
 * - ContactStepsPage (high-level orchestration via fixture injection)
 * And auto-waiting eliminates 60% of the code.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - 500+ lines for a single page object (Playwright equivalent: ~100 lines split across 2 files)
 * - Duplicated wait logic in every method
 * - Long method chains for form filling
 * - Manual handling of dynamic form sections
 * - Copy-paste patterns across similar methods (addStringAttribute, addNumberAttribute, etc.)
 * - Mix of concerns: creation + profile viewing + assertions in one class
 * - No composition - everything through inheritance
 *
 * @author QA Team
 * @since 2022-06
 * @see BasePage
 */
public class ContactPage extends BasePage {

    // ============================================================================================
    // LOCATORS SECTION
    // Note: 70+ locators for one page object. In Playwright, locators are defined inline
    // with the test using getByTestId(), getByRole(), etc. - no need to pre-declare them all.
    // ============================================================================================

    // ==================== Create Form: Header & Navigation ====================
    private static final By PAGE_TITLE = By.cssSelector("[data-auto-qa='contact-create-title']");
    private static final By SAVE_BUTTON = By.cssSelector("[data-auto-qa='contact-save-button']");
    private static final By CANCEL_BUTTON = By.cssSelector("[data-auto-qa='contact-cancel-button']");
    private static final By BACK_BUTTON = By.cssSelector("[data-auto-qa='contact-back-button']");
    private static final By SAVE_AND_NEW_BUTTON = By.cssSelector("[data-auto-qa='contact-save-and-new']");

    // ==================== Create Form: Channel Section ====================
    private static final By CHANNEL_SECTION = By.cssSelector("[data-auto-qa='channel-section']");
    private static final By EMAIL_CHANNEL_INPUT = By.cssSelector("[data-auto-qa='contact-email-input']");
    private static final By SMS_CHANNEL_INPUT = By.cssSelector("[data-auto-qa='contact-sms-input']");
    private static final By PUSH_TOKEN_INPUT = By.cssSelector("[data-auto-qa='contact-push-input']");
    private static final By SUBSCRIPTION_STATUS_DROPDOWN = By.cssSelector("[data-auto-qa='subscription-status']");
    private static final By ADD_CHANNEL_BUTTON = By.cssSelector("[data-auto-qa='add-channel-button']");
    private static final By CHANNEL_TYPE_DROPDOWN = By.cssSelector("[data-auto-qa='channel-type-select']");
    private static final By EMAIL_VALIDATION_ERROR = By.cssSelector("[data-auto-qa='email-validation-error']");
    private static final By PHONE_VALIDATION_ERROR = By.cssSelector("[data-auto-qa='phone-validation-error']");

    // ==================== Create Form: Attributes Section ====================
    private static final By ATTRIBUTES_SECTION = By.cssSelector("[data-auto-qa='attributes-section']");
    private static final By ADD_ATTRIBUTE_BUTTON = By.cssSelector("[data-auto-qa='add-attribute-button']");
    private static final By ATTRIBUTE_KEY_INPUT = By.cssSelector("[data-auto-qa='attribute-key-input']");
    private static final By ATTRIBUTE_VALUE_INPUT = By.cssSelector("[data-auto-qa='attribute-value-input']");
    private static final By ATTRIBUTE_TYPE_DROPDOWN = By.cssSelector("[data-auto-qa='attribute-type-select']");
    private static final By ATTRIBUTE_SAVE_BTN = By.cssSelector("[data-auto-qa='attribute-save']");
    private static final By ATTRIBUTE_CANCEL_BTN = By.cssSelector("[data-auto-qa='attribute-cancel']");
    private static final By ATTRIBUTE_DELETE_BTN = By.cssSelector("[data-auto-qa='attribute-delete']");

    // ==================== Create Form: Lists Section ====================
    private static final By LISTS_SECTION = By.cssSelector("[data-auto-qa='lists-section']");
    private static final By ADD_TO_LIST_BUTTON = By.cssSelector("[data-auto-qa='add-to-list-button']");
    private static final By LIST_SEARCH_INPUT = By.cssSelector("[data-auto-qa='list-search-input']");
    private static final By LIST_CHECKBOX = By.cssSelector("[data-auto-qa='list-checkbox']");
    private static final By LIST_CONFIRM_BUTTON = By.cssSelector("[data-auto-qa='list-confirm-button']");
    private static final By REMOVE_FROM_LIST_BUTTON = By.cssSelector("[data-auto-qa='remove-from-list']");

    // ==================== Create Form: Geo Attributes ====================
    private static final By GEO_COUNTRY_DROPDOWN = By.cssSelector("[data-auto-qa='geo-country']");
    private static final By GEO_TIMEZONE_DROPDOWN = By.cssSelector("[data-auto-qa='geo-timezone']");
    private static final By GEO_CITY_INPUT = By.cssSelector("[data-auto-qa='geo-city']");
    private static final By GEO_STATE_INPUT = By.cssSelector("[data-auto-qa='geo-state']");
    private static final By GEO_POSTAL_CODE_INPUT = By.cssSelector("[data-auto-qa='geo-postal-code']");
    private static final By GEO_LATITUDE_INPUT = By.cssSelector("[data-auto-qa='geo-latitude']");
    private static final By GEO_LONGITUDE_INPUT = By.cssSelector("[data-auto-qa='geo-longitude']");

    // ==================== Create Form: Date Attributes ====================
    private static final By DATE_PICKER_INPUT = By.cssSelector("[data-auto-qa='date-picker-input']");
    private static final By DATE_PICKER_CONFIRM = By.cssSelector("[data-auto-qa='date-picker-confirm']");
    private static final By DATE_PICKER_CANCEL = By.cssSelector("[data-auto-qa='date-picker-cancel']");

    // ==================== Create Form: Array Attributes ====================
    private static final By ARRAY_ADD_ITEM_BUTTON = By.cssSelector("[data-auto-qa='array-add-item']");
    private static final By ARRAY_ITEM_INPUT = By.cssSelector("[data-auto-qa='array-item-input']");
    private static final By ARRAY_REMOVE_ITEM = By.cssSelector("[data-auto-qa='array-remove-item']");

    // ==================== Profile Page: Header ====================
    private static final By PROFILE_HEADER = By.cssSelector("[data-auto-qa='contact-profile-header']");
    private static final By CONTACT_EMAIL_DISPLAY = By.cssSelector("[data-auto-qa='contact-email-value']");
    private static final By CONTACT_STATUS_DISPLAY = By.cssSelector("[data-auto-qa='contact-status-value']");
    private static final By CONTACT_ID_DISPLAY = By.cssSelector("[data-auto-qa='contact-id-value']");
    private static final By CONTACT_CREATED_DATE = By.cssSelector("[data-auto-qa='contact-created-date']");

    // ==================== Profile Page: Action Buttons ====================
    private static final By EDIT_BUTTON = By.cssSelector("[data-auto-qa='contact-edit-button']");
    private static final By DELETE_BUTTON = By.cssSelector("[data-auto-qa='contact-delete-button']");
    private static final By EXPORT_BUTTON = By.cssSelector("[data-auto-qa='contact-export-button']");

    // ==================== Profile Page: Tabs ====================
    private static final By ATTRIBUTES_TAB = By.cssSelector("[data-auto-qa='attributes-tab']");
    private static final By CHANNELS_TAB = By.cssSelector("[data-auto-qa='channels-tab']");
    private static final By LISTS_TAB = By.cssSelector("[data-auto-qa='lists-tab']");
    private static final By ACTIVITY_TAB = By.cssSelector("[data-auto-qa='activity-tab']");
    private static final By ORDERS_TAB = By.cssSelector("[data-auto-qa='orders-tab']");

    // ==================== Profile Page: Attribute Display ====================
    private static final By ATTRIBUTE_VALUE_BY_KEY = By.cssSelector("[data-auto-qa='attribute-row'] .attribute-value");
    private static final By ATTRIBUTE_ROW = By.cssSelector("[data-auto-qa='attribute-row']");
    private static final By ATTRIBUTE_INLINE_EDIT = By.cssSelector("[data-auto-qa='attribute-inline-edit']");

    // ==================== Profile Page: Activity ====================
    private static final By ACTIVITY_LIST = By.cssSelector("[data-auto-qa='activity-list']");
    private static final By ACTIVITY_ITEM = By.cssSelector("[data-auto-qa='activity-item']");
    private static final By ACTIVITY_FILTER = By.cssSelector("[data-auto-qa='activity-filter']");

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    public ContactPage(WebDriver driver) {
        super(driver);
    }

    // ============================================================================================
    // CREATE FORM: PAGE VERIFICATION
    // In Playwright: await expect(page.getByTestId('contact-create-title')).toBeVisible()
    // That's it. One line. No waitForPageLoad, no hardWait, no spinner check.
    // ============================================================================================

    /**
     * Verifies the contact creation form has fully loaded.
     * Must wait for:
     * 1. Page load complete (document.readyState)
     * 2. Hard wait for any remaining JS execution
     * 3. Title element visibility
     * 4. Save button visibility
     * 5. Channel section visibility
     * 6. Loading spinner to disappear
     *
     * In Playwright: toBeVisible() handles ALL of this automatically.
     */
    public void verifyContactCreatePageLoaded() {
        logger.info("Verifying contact create page is loaded");
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000); // Extra wait because page has async components
        WaitUtils.waitForElementVisible(driver, PAGE_TITLE);
        WaitUtils.waitForElementVisible(driver, SAVE_BUTTON);
        WaitUtils.waitForElementVisible(driver, CHANNEL_SECTION);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Contact create page loaded");
    }

    // ============================================================================================
    // CREATE FORM: CHANNEL METHODS
    // Notice: fillEmailChannel, fillSmsChannel, fillPushToken are nearly identical.
    // In Playwright: page.getByTestId('contact-email-input').fill(email) - one line, auto-waits.
    // ============================================================================================

    /**
     * Fills the email channel input field.
     * Requires: scroll → wait visible → wait clickable → clear → wait → type → wait for validation
     * In Playwright: await page.getByTestId('contact-email-input').fill(email);
     */
    public void fillEmailChannel(String email) {
        logger.info("Filling email channel: " + email);
        scrollToElement(CHANNEL_SECTION);
        WaitUtils.waitForElementVisible(driver, EMAIL_CHANNEL_INPUT);
        WaitUtils.hardWait(500); // Wait for input to be ready after scroll animation
        WebElement emailInput = WaitUtils.waitForElementClickable(driver, EMAIL_CHANNEL_INPUT);
        emailInput.clear();
        WaitUtils.hardWait(200); // Wait after clear to avoid race condition
        emailInput.sendKeys(email);
        WaitUtils.hardWait(500); // Wait for email validation to complete
        ReportManager.logStep("Filled email: " + email);
    }

    /**
     * Fills the SMS channel input field.
     * Same pattern as fillEmailChannel - copy-pasted with different locator.
     * In Playwright: await page.getByTestId('contact-sms-input').fill(phone);
     */
    public void fillSmsChannel(String phone) {
        logger.info("Filling SMS channel: " + phone);
        scrollToElement(SMS_CHANNEL_INPUT);
        WaitUtils.waitForElementVisible(driver, SMS_CHANNEL_INPUT);
        WaitUtils.hardWait(500); // Same arbitrary wait
        WebElement smsInput = WaitUtils.waitForElementClickable(driver, SMS_CHANNEL_INPUT);
        smsInput.clear();
        WaitUtils.hardWait(200); // Same arbitrary wait
        smsInput.sendKeys(phone);
        WaitUtils.hardWait(500); // Same arbitrary wait
        ReportManager.logStep("Filled SMS: " + phone);
    }

    /**
     * Fills the push notification token input.
     * Same copy-paste pattern AGAIN.
     */
    public void fillPushToken(String token) {
        logger.info("Filling push token");
        scrollToElement(PUSH_TOKEN_INPUT);
        WaitUtils.waitForElementVisible(driver, PUSH_TOKEN_INPUT);
        WaitUtils.hardWait(500);
        WebElement pushInput = WaitUtils.waitForElementClickable(driver, PUSH_TOKEN_INPUT);
        pushInput.clear();
        WaitUtils.hardWait(200);
        pushInput.sendKeys(token);
        WaitUtils.hardWait(500);
        ReportManager.logStep("Filled push token");
    }

    /**
     * Selects subscription status from dropdown.
     * Requires scroll + wait + select + wait. Every. Single. Dropdown.
     */
    public void selectSubscriptionStatus(String status) {
        logger.info("Selecting subscription status: " + status);
        scrollToElement(SUBSCRIPTION_STATUS_DROPDOWN);
        WaitUtils.waitForElementVisible(driver, SUBSCRIPTION_STATUS_DROPDOWN);
        WaitUtils.hardWait(500);
        selectDropdownByText(SUBSCRIPTION_STATUS_DROPDOWN, status);
        WaitUtils.hardWait(500); // Wait for UI to reflect selection
        ReportManager.logStep("Selected subscription status: " + status);
    }

    /**
     * Adds a new channel type (email, sms, push) to the contact.
     * Demonstrates: click button → wait for modal → select type → wait → confirm
     */
    public void addNewChannel(String channelType) {
        logger.info("Adding new channel: " + channelType);
        scrollToElement(ADD_CHANNEL_BUTTON);
        WaitUtils.waitForElementClickable(driver, ADD_CHANNEL_BUTTON);
        WaitUtils.hardWait(300);
        click(ADD_CHANNEL_BUTTON);
        WaitUtils.hardWait(1000); // Wait for channel type modal/dropdown to appear
        selectDropdownByText(CHANNEL_TYPE_DROPDOWN, channelType);
        WaitUtils.hardWait(500);
        ReportManager.logStep("Added new channel type: " + channelType);
    }

    // ============================================================================================
    // CREATE FORM: ATTRIBUTE METHODS
    //
    // THIS IS THE WORST PART. Look at addStringAttribute, addNumberAttribute, addDateAttribute,
    // addArrayAttribute below. They are ALL the same pattern:
    //   1. Scroll to section
    //   2. Wait 500ms
    //   3. Click add button
    //   4. Wait 1000ms
    //   5. Fill key input (wait 300ms after)
    //   6. Select type from dropdown (wait 500ms after)
    //   7. Fill value (wait 300ms after)
    //   8. Click save (wait 1000ms after)
    //   9. Wait for spinner
    //
    // In Playwright this would be ONE method with a type parameter, and ZERO waits:
    //   async addAttribute(key: string, type: string, value: string) {
    //     await this.page.getByTestId('add-attribute-button').click();
    //     await this.page.getByTestId('attribute-key-input').fill(key);
    //     await this.page.getByTestId('attribute-type-select').selectOption(type);
    //     await this.page.getByTestId('attribute-value-input').fill(value);
    //     await this.page.getByTestId('attribute-save').click();
    //   }
    // ============================================================================================

    /**
     * Adds a string attribute to the contact.
     * 15 lines of code for what Playwright does in 5 lines with zero waits.
     */
    public void addStringAttribute(String key, String value) {
        logger.info("Adding string attribute: " + key + " = " + value);
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_ATTRIBUTE_BUTTON);
        WaitUtils.hardWait(1000); // Wait for attribute form to appear

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_KEY_INPUT);
        WaitUtils.hardWait(300);
        type(ATTRIBUTE_KEY_INPUT, key);
        WaitUtils.hardWait(300);

        selectDropdownByText(ATTRIBUTE_TYPE_DROPDOWN, "String");
        WaitUtils.hardWait(500);

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_VALUE_INPUT);
        WaitUtils.hardWait(300);
        type(ATTRIBUTE_VALUE_INPUT, value);
        WaitUtils.hardWait(300);

        click(ATTRIBUTE_SAVE_BTN);
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added string attribute: " + key);
    }

    /**
     * Adds a number attribute. SAME PATTERN as addStringAttribute with "Number" instead of "String".
     * This is the copy-paste problem.
     */
    public void addNumberAttribute(String key, String value) {
        logger.info("Adding number attribute: " + key + " = " + value);
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_ATTRIBUTE_BUTTON);
        WaitUtils.hardWait(1000); // Same wait

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_KEY_INPUT);
        WaitUtils.hardWait(300); // Same wait
        type(ATTRIBUTE_KEY_INPUT, key);
        WaitUtils.hardWait(300); // Same wait

        selectDropdownByText(ATTRIBUTE_TYPE_DROPDOWN, "Number"); // Only this changes!
        WaitUtils.hardWait(500); // Same wait

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_VALUE_INPUT);
        WaitUtils.hardWait(300); // Same wait
        type(ATTRIBUTE_VALUE_INPUT, value);
        WaitUtils.hardWait(300); // Same wait

        click(ATTRIBUTE_SAVE_BTN);
        WaitUtils.hardWait(1000); // Same wait
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added number attribute: " + key);
    }

    /**
     * Adds a boolean attribute. YES, SAME PATTERN AGAIN.
     * At this point you can see the code smell.
     */
    public void addBooleanAttribute(String key, boolean value) {
        logger.info("Adding boolean attribute: " + key + " = " + value);
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_ATTRIBUTE_BUTTON);
        WaitUtils.hardWait(1000);

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_KEY_INPUT);
        WaitUtils.hardWait(300);
        type(ATTRIBUTE_KEY_INPUT, key);
        WaitUtils.hardWait(300);

        selectDropdownByText(ATTRIBUTE_TYPE_DROPDOWN, "Boolean");
        WaitUtils.hardWait(500);

        // Boolean uses a toggle instead of text input - slightly different but still copy-paste
        By toggleLocator = By.cssSelector("[data-auto-qa='attribute-boolean-toggle']");
        WaitUtils.waitForElementClickable(driver, toggleLocator);
        if (value) {
            click(toggleLocator);
        }
        WaitUtils.hardWait(300);

        click(ATTRIBUTE_SAVE_BTN);
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added boolean attribute: " + key + " = " + value);
    }

    /**
     * Adds a date attribute. Same pattern + date picker complexity.
     */
    public void addDateAttribute(String key, String dateValue) {
        logger.info("Adding date attribute: " + key + " = " + dateValue);
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_ATTRIBUTE_BUTTON);
        WaitUtils.hardWait(1000);

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_KEY_INPUT);
        WaitUtils.hardWait(300);
        type(ATTRIBUTE_KEY_INPUT, key);
        WaitUtils.hardWait(300);

        selectDropdownByText(ATTRIBUTE_TYPE_DROPDOWN, "Date");
        WaitUtils.hardWait(500);

        // Date picker adds extra complexity - must interact with calendar widget
        WaitUtils.waitForElementVisible(driver, DATE_PICKER_INPUT);
        WaitUtils.hardWait(300);
        type(DATE_PICKER_INPUT, dateValue);
        WaitUtils.hardWait(300);
        click(DATE_PICKER_CONFIRM);
        WaitUtils.hardWait(500); // Wait for date picker to close

        click(ATTRIBUTE_SAVE_BTN);
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added date attribute: " + key);
    }

    /**
     * Adds an array attribute. Same base pattern + loop for multiple values.
     */
    public void addArrayAttribute(String key, List<String> values) {
        logger.info("Adding array attribute: " + key + " with " + values.size() + " items");
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_ATTRIBUTE_BUTTON);
        WaitUtils.hardWait(1000);

        WaitUtils.waitForElementVisible(driver, ATTRIBUTE_KEY_INPUT);
        WaitUtils.hardWait(300);
        type(ATTRIBUTE_KEY_INPUT, key);
        WaitUtils.hardWait(300);

        selectDropdownByText(ATTRIBUTE_TYPE_DROPDOWN, "Array");
        WaitUtils.hardWait(500);

        // Loop through values - each requires its own click + wait + type
        for (String value : values) {
            click(ARRAY_ADD_ITEM_BUTTON);
            WaitUtils.hardWait(500); // Wait for new input to render
            // Find last array input and type into it
            List<WebElement> inputs = driver.findElements(ARRAY_ITEM_INPUT);
            WebElement lastInput = inputs.get(inputs.size() - 1);
            WaitUtils.waitForElementVisible(driver, lastInput);
            WaitUtils.hardWait(200); // Wait before typing
            lastInput.sendKeys(value);
            WaitUtils.hardWait(300); // Wait after typing
        }

        click(ATTRIBUTE_SAVE_BTN);
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added array attribute: " + key);
    }

    /**
     * Adds geo/location attributes. Multiple fields, multiple waits.
     * In Playwright this would be a simple sequential fill without any waits.
     */
    public void addGeoAttribute(String country, String timezone, String city, String state, String postalCode) {
        logger.info("Adding geo attributes");
        scrollToElement(ATTRIBUTES_SECTION);
        WaitUtils.hardWait(500);

        // Country - selecting triggers async timezone list update
        WaitUtils.waitForElementVisible(driver, GEO_COUNTRY_DROPDOWN);
        WaitUtils.hardWait(300);
        selectDropdownByText(GEO_COUNTRY_DROPDOWN, country);
        WaitUtils.hardWait(1000); // MUST wait for timezone options to reload via AJAX

        // Timezone - depends on country selection above
        WaitUtils.waitForElementVisible(driver, GEO_TIMEZONE_DROPDOWN);
        WaitUtils.hardWait(300);
        selectDropdownByText(GEO_TIMEZONE_DROPDOWN, timezone);
        WaitUtils.hardWait(500);

        // City
        WaitUtils.waitForElementVisible(driver, GEO_CITY_INPUT);
        WaitUtils.hardWait(300);
        type(GEO_CITY_INPUT, city);
        WaitUtils.hardWait(300);

        // State
        WaitUtils.waitForElementVisible(driver, GEO_STATE_INPUT);
        WaitUtils.hardWait(300);
        type(GEO_STATE_INPUT, state);
        WaitUtils.hardWait(300);

        // Postal Code
        WaitUtils.waitForElementVisible(driver, GEO_POSTAL_CODE_INPUT);
        WaitUtils.hardWait(300);
        type(GEO_POSTAL_CODE_INPUT, postalCode);
        WaitUtils.hardWait(300);

        ReportManager.logStep("Added geo attributes: " + country + ", " + city);
    }

    /**
     * Deletes an existing attribute by key name.
     */
    public void deleteAttribute(String key) {
        logger.info("Deleting attribute: " + key);
        By attributeRow = By.xpath("//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]");
        scrollToElement(attributeRow);
        WaitUtils.hardWait(500);
        hoverElement(attributeRow);
        WaitUtils.hardWait(500); // Wait for delete icon to appear on hover

        By deleteIcon = By.xpath("//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]//button[@data-auto-qa='attribute-delete']");
        WaitUtils.waitForElementClickable(driver, deleteIcon);
        WaitUtils.hardWait(300);
        click(deleteIcon);
        WaitUtils.hardWait(1000);
        confirmAlertModal();
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Deleted attribute: " + key);
    }

    // ============================================================================================
    // CREATE FORM: LIST METHODS
    // ============================================================================================

    /**
     * Adds the contact to a list by searching and selecting.
     */
    public void addToList(String listName) {
        logger.info("Adding contact to list: " + listName);
        scrollToElement(LISTS_SECTION);
        WaitUtils.hardWait(500);
        click(ADD_TO_LIST_BUTTON);
        WaitUtils.hardWait(1000); // Wait for list modal to open

        WaitUtils.waitForElementVisible(driver, LIST_SEARCH_INPUT);
        WaitUtils.hardWait(300);
        type(LIST_SEARCH_INPUT, listName);
        WaitUtils.hardWait(1500); // Wait for search results to filter (debounced search)

        // Click the checkbox for the matching list
        click(LIST_CHECKBOX);
        WaitUtils.hardWait(500);

        click(LIST_CONFIRM_BUTTON);
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Added to list: " + listName);
    }

    /**
     * Removes the contact from a list.
     */
    public void removeFromList(String listName) {
        logger.info("Removing contact from list: " + listName);
        // First open lists tab if on profile page
        By listRow = By.xpath("//div[@data-auto-qa='list-row' and .//span[text()='" + listName + "']]");
        scrollToElement(listRow);
        WaitUtils.hardWait(500);
        hoverElement(listRow);
        WaitUtils.hardWait(500);

        By removeBtn = By.xpath("//div[@data-auto-qa='list-row' and .//span[text()='" + listName + "']]//button[@data-auto-qa='remove-from-list']");
        WaitUtils.waitForElementClickable(driver, removeBtn);
        click(removeBtn);
        WaitUtils.hardWait(1000);
        confirmAlertModal();
        WaitUtils.hardWait(1000);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Removed from list: " + listName);
    }

    // ============================================================================================
    // CREATE FORM: SAVE & CANCEL
    // ============================================================================================

    /**
     * Clicks save button.
     * Requires: scroll to top → wait → wait for clickable → wait → click → wait for API → wait for spinner
     */
    public void clickSaveButton() {
        logger.info("Clicking save button");
        scrollToTop();
        WaitUtils.hardWait(500);
        WaitUtils.waitForElementClickable(driver, SAVE_BUTTON);
        WaitUtils.hardWait(300);
        click(SAVE_BUTTON);
        WaitUtils.hardWait(2000); // Wait for save API call to complete
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Clicked save button");
    }

    /**
     * Saves contact and verifies success toast appears.
     */
    public void saveContactAndVerifySuccess() {
        clickSaveButton();
        waitForSuccessToast();
        WaitUtils.hardWait(1000); // Additional wait after toast for page state to settle
        ReportManager.logStep("Contact saved successfully");
    }

    /**
     * Clicks cancel and handles unsaved changes modal if present.
     */
    public void clickCancelButton() {
        WaitUtils.waitForElementClickable(driver, CANCEL_BUTTON);
        click(CANCEL_BUTTON);
        WaitUtils.hardWait(1000);
        // May trigger "unsaved changes" confirmation
        try {
            if (isElementDisplayed(MODAL_CONFIRM_BTN)) {
                confirmAlertModal();
                WaitUtils.hardWait(1000);
            }
        } catch (Exception e) {
            // No modal appeared, continue
        }
    }

    // ============================================================================================
    // PROFILE PAGE: VERIFICATION
    // This section handles the profile VIEW - mixed in with the creation form above.
    // In Playwright, this would be a SEPARATE page object (ContactProfilePage).
    // ============================================================================================

    /**
     * Verifies the contact profile page has loaded.
     * Same verbose pattern as verifyContactCreatePageLoaded().
     */
    public void verifyProfileLoaded() {
        logger.info("Verifying contact profile is loaded");
        WaitUtils.waitForPageLoad(driver);
        WaitUtils.hardWait(2000); // Profile has async widgets loading
        WaitUtils.waitForElementVisible(driver, PROFILE_HEADER);
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Contact profile loaded");
    }

    /**
     * Gets the displayed email on the profile page.
     */
    public String getContactEmail() {
        WaitUtils.waitForElementVisible(driver, CONTACT_EMAIL_DISPLAY);
        WaitUtils.hardWait(500); // Wait for value to render
        return driver.findElement(CONTACT_EMAIL_DISPLAY).getText();
    }

    /**
     * Gets the subscription status displayed on profile.
     */
    public String getContactStatus() {
        WaitUtils.waitForElementVisible(driver, CONTACT_STATUS_DISPLAY);
        WaitUtils.hardWait(500);
        return driver.findElement(CONTACT_STATUS_DISPLAY).getText();
    }

    /**
     * Gets the contact ID from profile header.
     */
    public String getContactId() {
        WaitUtils.waitForElementVisible(driver, CONTACT_ID_DISPLAY);
        WaitUtils.hardWait(500);
        return driver.findElement(CONTACT_ID_DISPLAY).getText();
    }

    // ============================================================================================
    // PROFILE PAGE: ACTIONS
    // ============================================================================================

    /**
     * Clicks the edit button on the profile to open the edit form.
     */
    public void clickEditButton() {
        WaitUtils.waitForElementClickable(driver, EDIT_BUTTON);
        WaitUtils.hardWait(300);
        click(EDIT_BUTTON);
        WaitUtils.hardWait(1000); // Wait for edit form to load
        waitForLoadingSpinnerToDisappear();
    }

    /**
     * Deletes the contact from the profile page.
     * Requires: wait → click → wait for modal → confirm → wait → verify toast
     */
    public void deleteContact() {
        logger.info("Deleting contact");
        WaitUtils.waitForElementClickable(driver, DELETE_BUTTON);
        WaitUtils.hardWait(500);
        click(DELETE_BUTTON);
        WaitUtils.hardWait(1000); // Wait for confirmation modal to appear
        confirmAlertModal();
        WaitUtils.hardWait(2000); // Wait for delete API call
        waitForSuccessToast();
        ReportManager.logStep("Contact deleted");
    }

    /**
     * Exports contact data (CSV/JSON).
     */
    public void exportContact(String format) {
        logger.info("Exporting contact as: " + format);
        WaitUtils.waitForElementClickable(driver, EXPORT_BUTTON);
        WaitUtils.hardWait(300);
        click(EXPORT_BUTTON);
        WaitUtils.hardWait(1000); // Wait for export options dropdown
        By formatOption = By.cssSelector("[data-auto-qa='export-" + format.toLowerCase() + "']");
        WaitUtils.waitForElementClickable(driver, formatOption);
        click(formatOption);
        WaitUtils.hardWait(2000); // Wait for file download to start
        ReportManager.logStep("Exported contact as: " + format);
    }

    // ============================================================================================
    // PROFILE PAGE: TAB NAVIGATION
    // Each tab click requires: wait clickable → wait → click → wait → wait for spinner
    // In Playwright: await page.getByTestId('attributes-tab').click() — done.
    // ============================================================================================

    public void openAttributesTab() {
        WaitUtils.waitForElementClickable(driver, ATTRIBUTES_TAB);
        WaitUtils.hardWait(300);
        click(ATTRIBUTES_TAB);
        WaitUtils.hardWait(1500); // Wait for tab content to load via AJAX
        waitForLoadingSpinnerToDisappear();
    }

    public void openChannelsTab() {
        WaitUtils.waitForElementClickable(driver, CHANNELS_TAB);
        WaitUtils.hardWait(300);
        click(CHANNELS_TAB);
        WaitUtils.hardWait(1500);
        waitForLoadingSpinnerToDisappear();
    }

    public void openListsTab() {
        WaitUtils.waitForElementClickable(driver, LISTS_TAB);
        WaitUtils.hardWait(300);
        click(LISTS_TAB);
        WaitUtils.hardWait(1500);
        waitForLoadingSpinnerToDisappear();
    }

    public void openActivityTab() {
        WaitUtils.waitForElementClickable(driver, ACTIVITY_TAB);
        WaitUtils.hardWait(300);
        click(ACTIVITY_TAB);
        WaitUtils.hardWait(2000); // Activity tab loads more data
        waitForLoadingSpinnerToDisappear();
    }

    public void openOrdersTab() {
        WaitUtils.waitForElementClickable(driver, ORDERS_TAB);
        WaitUtils.hardWait(300);
        click(ORDERS_TAB);
        WaitUtils.hardWait(1500);
        waitForLoadingSpinnerToDisappear();
    }

    // ============================================================================================
    // PROFILE PAGE: ASSERTIONS
    // Manual assertion methods with waits - Playwright's expect() auto-retries.
    // ============================================================================================

    /**
     * Verifies an attribute value on the profile page.
     * Must build XPath dynamically, wait for it, then manually compare strings.
     * In Playwright: await expect(page.getByTestId('attr-' + key)).toHaveText(expectedValue);
     */
    public void verifyAttributeValue(String key, String expectedValue) {
        logger.info("Verifying attribute: " + key + " = " + expectedValue);
        By attributeLocator = By.xpath(
                "//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]" +
                        "//span[@class='attribute-value']"
        );
        WaitUtils.waitForElementVisible(driver, attributeLocator);
        WaitUtils.hardWait(500); // Wait for value to fully render
        String actualValue = driver.findElement(attributeLocator).getText();
        if (!actualValue.equals(expectedValue)) {
            throw new AssertionError(
                    "Attribute '" + key + "' expected '" + expectedValue + "' but found '" + actualValue + "'"
            );
        }
        ReportManager.logStep("Verified attribute: " + key + " = " + expectedValue);
    }

    /**
     * Verifies multiple attributes in one go.
     * Must loop and call verifyAttributeValue for each - more boilerplate.
     */
    public void verifyAttributes(String[][] keyValuePairs) {
        for (String[] pair : keyValuePairs) {
            verifyAttributeValue(pair[0], pair[1]);
        }
    }

    /**
     * Checks if contact belongs to a specific list.
     * Must switch tabs, build locator, wait, then check.
     */
    public boolean isContactInList(String listName) {
        openListsTab();
        By listLocator = By.xpath(
                "//div[@data-auto-qa='list-row' and .//span[text()='" + listName + "']]"
        );
        try {
            WaitUtils.waitForElementVisible(driver, listLocator, 10);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets count of lists the contact belongs to.
     */
    public int getListCount() {
        openListsTab();
        WaitUtils.hardWait(1000);
        List<WebElement> listRows = driver.findElements(By.cssSelector("[data-auto-qa='list-row']"));
        return listRows.size();
    }

    /**
     * Checks if a specific activity exists in the activity tab.
     */
    public boolean hasActivity(String activityType) {
        openActivityTab();
        By activityLocator = By.xpath(
                "//div[@data-auto-qa='activity-item' and contains(., '" + activityType + "')]"
        );
        try {
            WaitUtils.waitForElementVisible(driver, activityLocator, 10);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================================================
    // PROFILE PAGE: INLINE EDIT
    // Edit attributes directly from the profile page (without opening full edit form)
    // ============================================================================================

    /**
     * Edits an attribute value inline on the profile page.
     * Requires: find row → hover → click edit icon → wait → clear → type → save → wait
     */
    public void inlineEditAttribute(String key, String newValue) {
        logger.info("Inline editing attribute: " + key + " → " + newValue);
        By attributeRow = By.xpath("//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]");
        scrollToElement(attributeRow);
        WaitUtils.hardWait(500);

        // Hover to reveal edit icon
        hoverElement(attributeRow);
        WaitUtils.hardWait(500);

        // Click edit icon
        By editIcon = By.xpath("//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]//button[@data-auto-qa='attribute-inline-edit']");
        WaitUtils.waitForElementClickable(driver, editIcon);
        WaitUtils.hardWait(300);
        click(editIcon);
        WaitUtils.hardWait(500); // Wait for inline edit mode to activate

        // Find input field within the row and update
        By inlineInput = By.xpath("//div[@data-auto-qa='attribute-row' and .//span[text()='" + key + "']]//input");
        WaitUtils.waitForElementVisible(driver, inlineInput);
        WebElement input = driver.findElement(inlineInput);
        input.sendKeys(Keys.CONTROL + "a");
        WaitUtils.hardWait(100);
        input.sendKeys(Keys.DELETE);
        WaitUtils.hardWait(200);
        input.sendKeys(newValue);
        WaitUtils.hardWait(300);

        // Press Enter to save
        input.sendKeys(Keys.ENTER);
        WaitUtils.hardWait(1000); // Wait for save API call
        waitForLoadingSpinnerToDisappear();
        ReportManager.logStep("Inline edited: " + key + " → " + newValue);
    }

    // ============================================================================================
    // HELPER / UTILITY METHODS
    // ============================================================================================

    /**
     * Gets the page title text.
     */
    public String getPageTitle() {
        WaitUtils.waitForElementVisible(driver, PAGE_TITLE);
        return driver.findElement(PAGE_TITLE).getText();
    }

    /**
     * Checks if the email validation error is displayed.
     */
    public boolean isEmailValidationErrorDisplayed() {
        try {
            WaitUtils.hardWait(1000); // Wait for validation to trigger
            return driver.findElement(EMAIL_VALIDATION_ERROR).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the save button is enabled (form is valid).
     */
    public boolean isSaveButtonEnabled() {
        WaitUtils.waitForElementVisible(driver, SAVE_BUTTON);
        return driver.findElement(SAVE_BUTTON).isEnabled();
    }
}
