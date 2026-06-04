package com.acmeplatform.automation.tests;

import com.acmeplatform.automation.base.BaseTest;
import com.acmeplatform.automation.pages.*;
import com.acmeplatform.automation.utils.ConfigReader;
import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * ContactTest - Contact CRUD test cases.
 * 
 * Pain points demonstrated:
 * - Long @Test methods (~80+ lines each) because there are no fixtures for setup/teardown
 * - Manual page navigation and verification at every step
 * - No reusable test context (each test starts from scratch)
 * - Inline test data generation (no data builders injected via fixtures)
 * - Sequential execution forced by shared state concerns
 * - Must login in EVERY test because there's no shared session fixture
 * - Page objects must be manually instantiated in @BeforeMethod
 *
 * IN PLAYWRIGHT:
 * - Fixtures inject page objects and authenticated session automatically
 * - Test data created via API in globalSetup (shared across all tests)
 * - Each test is ~20 lines instead of 80+
 * - Tests run in parallel with isolated browser contexts
 *
 * Compare this with: playwright-automation-framework/tests/ui/specs/contacts.spec.ts
 * The Playwright version is 60% shorter and requires zero login/navigation boilerplate.
 */
public class ContactTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private ContactPage contactPage; // One bloated page object handles everything

    @BeforeMethod
    public void initPages() {
        // Must manually instantiate every page object
        // In Playwright: fixtures inject a PageManager with all pages ready
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        contactPage = new ContactPage(driver);
    }

    @Test(description = "Create a new contact with email channel and multiple attributes")
    public void testCreateContactWithEmailAndAttributes() {
        ReportManager.logStep("Starting contact creation test");

        // =====================================================================
        // LOGIN BOILERPLATE - Must repeat in EVERY test.
        // In Playwright: session is stored in storageState and reused.
        // This alone adds 5 lines + 2 seconds to every single test.
        // =====================================================================
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.login(email, password);
        WaitUtils.hardWait(2000); // Why 2000ms? Nobody remembers. But removing it breaks things.

        // =====================================================================
        // NAVIGATION BOILERPLATE - Must manually verify each page transition.
        // In Playwright: await page.goto('/contacts/create') — auto-waits for load.
        // =====================================================================
        dashboardPage.verifyDashboardLoaded();
        dashboardPage.navigateToContacts();
        WaitUtils.hardWait(2000);

        // Open create contact form
        navigateToUrl(baseUrl + "/contacts/create");
        WaitUtils.hardWait(3000); // 3 full seconds of doing nothing. Flaky without it.
        WaitUtils.waitForPageLoad(driver);

        // Verify page loaded
        contactPage.verifyContactCreatePageLoaded();

        // =====================================================================
        // INLINE TEST DATA - No fixtures, no builders, no API pre-creation.
        // In Playwright: test data is created in globalSetup via API,
        // and injected into the test via fixtures.
        // =====================================================================
        String contactEmail = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@automation.test";
        String firstName = "AutoTest";
        String lastName = "Contact_" + System.currentTimeMillis();
        String age = "30";

        // Fill channel information
        contactPage.fillEmailChannel(contactEmail);
        contactPage.selectSubscriptionStatus("Subscribed");
        WaitUtils.hardWait(1000);

        // Fill string attributes (notice: each call has ~8 internal waits)
        contactPage.addStringAttribute("first_name", firstName);
        WaitUtils.hardWait(500); // Wait between attributes because form state is unpredictable
        contactPage.addStringAttribute("last_name", lastName);
        WaitUtils.hardWait(500);

        // Fill number attribute (same code as string, just different dropdown selection)
        contactPage.addNumberAttribute("age", age);
        WaitUtils.hardWait(500);

        // Fill date attribute (adds date picker complexity)
        contactPage.addDateAttribute("birthday", "1993-05-15");
        WaitUtils.hardWait(500);

        // Fill array attribute (loop with waits per item)
        contactPage.addArrayAttribute("interests", Arrays.asList("technology", "sports", "music"));
        WaitUtils.hardWait(500);

        // Fill geo attributes (5 fields, each with its own wait)
        contactPage.addGeoAttribute("United States", "America/Los_Angeles",
                "San Francisco", "CA", "94105");
        WaitUtils.hardWait(500);

        // Add to list
        contactPage.addToList("automation-test-list");
        WaitUtils.hardWait(1000);

        // Save contact
        contactPage.saveContactAndVerifySuccess();
        WaitUtils.hardWait(3000); // Wait for redirect to profile (no way to detect this reliably)

        // =====================================================================
        // ASSERTIONS - Manual verification, no auto-retrying.
        // In Playwright: await expect(locator).toHaveText(value) retries automatically.
        // =====================================================================
        contactPage.verifyProfileLoaded();
        String savedEmail = contactPage.getContactEmail();
        Assert.assertEquals(savedEmail, contactEmail,
                "Contact email should match. Expected: " + contactEmail + ", Actual: " + savedEmail);

        // Verify attributes (each opens a tab, waits, then checks)
        contactPage.openAttributesTab();
        WaitUtils.hardWait(1500);
        contactPage.verifyAttributeValue("first_name", firstName);
        contactPage.verifyAttributeValue("last_name", lastName);
        contactPage.verifyAttributeValue("age", age);

        // Verify list membership
        Assert.assertTrue(contactPage.isContactInList("automation-test-list"),
                "Contact should be in automation-test-list");

        ReportManager.logStep("Contact created and verified successfully");
    }

    @Test(description = "Update an existing contact's attributes")
    public void testUpdateContactAttributes() {
        ReportManager.logStep("Starting contact update test");

        // Login AGAIN (same boilerplate as every other test)
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.login(email, password);
        WaitUtils.hardWait(2000);

        // Navigate to an existing contact
        // PROBLEM: No API fixture creates this contact, so we hardcode an ID.
        // If test data is cleaned up, this test breaks silently.
        navigateToUrl(baseUrl + "/contacts/profile/test-contact-id");
        WaitUtils.hardWait(3000);
        WaitUtils.waitForPageLoad(driver);

        // Verify profile loaded
        contactPage.verifyProfileLoaded();

        // Click edit
        contactPage.clickEditButton();
        WaitUtils.hardWait(2000);

        // Update attributes
        String newFirstName = "Updated_" + System.currentTimeMillis();
        contactPage.addStringAttribute("first_name", newFirstName);
        WaitUtils.hardWait(500);

        // Save
        contactPage.saveContactAndVerifySuccess();
        WaitUtils.hardWait(3000);

        // Verify update
        contactPage.verifyProfileLoaded();
        contactPage.openAttributesTab();
        WaitUtils.hardWait(1500);
        contactPage.verifyAttributeValue("first_name", newFirstName);

        ReportManager.logStep("Contact updated successfully");
    }

    @Test(description = "Delete a contact and verify removal")
    public void testDeleteContact() {
        ReportManager.logStep("Starting contact deletion test");

        // Login (3rd time in this test class...)
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.login(email, password);
        WaitUtils.hardWait(2000);

        // First create a contact to delete
        // PROBLEM: No API fixture for pre-conditions.
        // In Playwright: globalSetup creates test data via API before any test runs.
        // Here we must create via UI first, then delete — making the test 2x longer.
        navigateToUrl(baseUrl + "/contacts/create");
        WaitUtils.hardWait(3000);
        contactPage.verifyContactCreatePageLoaded();

        String contactEmail = "delete-me-" + UUID.randomUUID().toString().substring(0, 8) + "@automation.test";
        contactPage.fillEmailChannel(contactEmail);
        contactPage.selectSubscriptionStatus("Subscribed");
        contactPage.saveContactAndVerifySuccess();
        WaitUtils.hardWait(3000);

        // Now delete the contact
        contactPage.verifyProfileLoaded();
        contactPage.deleteContact();
        WaitUtils.hardWait(2000);

        // Verify redirect to contacts list
        WaitUtils.waitForUrlContains(driver, "/contacts");
        ReportManager.logStep("Contact deleted successfully");
    }
}
