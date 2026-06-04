package com.acmeplatform.automation.api;

import com.acmeplatform.automation.utils.ConfigReader;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

/**
 * ContactApiTest - API tests for contact management.
 * 
 * Pain points demonstrated:
 * - Cannot reuse UI browser session cookies for API calls
 * - Separate authentication flow from UI tests
 * - If you need to verify API-created data in UI, you need a completely separate test
 * - No shared context between API and UI layers (Playwright's request fixture solves this)
 * - Duplicate test data management
 */
public class ContactApiTest extends ApiBase {

    @BeforeClass
    public void setupApiTests() {
        ConfigReader.loadConfig(System.getProperty("environment", "qc"));
        setupApi(
                ConfigReader.getApiBaseUrl(),
                ConfigReader.getApiKey(),
                ConfigReader.getProperty("account.id")
        );
        authenticate(); // Separate auth - can't use browser session!
    }

    @Test(description = "Create a contact via API")
    public void testCreateContactViaApi() {
        logger.info("Creating contact via API (separate from UI session)");

        String randomEmail = "api-test-" + UUID.randomUUID().toString().substring(0, 8) + "@automation.test";

        // Build request payload manually (no shared data builders with UI tests)
        Map<String, Object> channels = new HashMap<>();
        Map<String, Object> emailChannel = new HashMap<>();
        emailChannel.put("address", randomEmail);
        emailChannel.put("subscribeStatus", "subscribed");
        channels.put("email", emailChannel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("channels", channels);
        payload.put("first_name", "API_Test");
        payload.put("last_name", "Contact_" + System.currentTimeMillis());

        // Make API call
        Response response = post("/contacts", payload);

        // Assertions
        Assert.assertEquals(response.getStatusCode(), 200,
                "Contact creation should return 200. Response: " + response.getBody().asString());

        String contactId = response.jsonPath().getString("_id");
        Assert.assertNotNull(contactId, "Response should contain contact ID");

        logger.info("Contact created via API. ID: " + contactId);

        // Verify by GET (still via API - can't easily check this in the UI without a separate Selenium test)
        Response getResponse = get("/contacts/" + contactId);
        Assert.assertEquals(getResponse.getStatusCode(), 200);
        Assert.assertEquals(getResponse.jsonPath().getString("channels.email.address"), randomEmail);
    }

    @Test(description = "Create a multichannel contact via API")
    public void testCreateMultichannelContactViaApi() {
        logger.info("Creating multichannel contact via API");

        String randomEmail = "multi-" + UUID.randomUUID().toString().substring(0, 8) + "@automation.test";
        String randomPhone = "+1555" + (1000000 + new Random().nextInt(9000000));

        // Build complex payload
        Map<String, Object> channels = new HashMap<>();

        Map<String, Object> emailChannel = new HashMap<>();
        emailChannel.put("address", randomEmail);
        emailChannel.put("subscribeStatus", "subscribed");
        channels.put("email", emailChannel);

        Map<String, Object> smsChannel = new HashMap<>();
        smsChannel.put("address", randomPhone);
        smsChannel.put("subscribeStatus", "subscribed");
        channels.put("sms", smsChannel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("channels", channels);
        payload.put("first_name", "Multi");
        payload.put("last_name", "Channel_" + System.currentTimeMillis());

        // Create contact
        Response response = post("/contacts", payload);
        Assert.assertEquals(response.getStatusCode(), 200);

        String contactId = response.jsonPath().getString("_id");
        Assert.assertNotNull(contactId);

        // Verify both channels
        Response getResponse = get("/contacts/" + contactId);
        Assert.assertEquals(getResponse.jsonPath().getString("channels.email.address"), randomEmail);
        Assert.assertEquals(getResponse.jsonPath().getString("channels.sms.address"), randomPhone);

        logger.info("Multichannel contact created. ID: " + contactId);
    }

    @Test(description = "Delete a contact via API")
    public void testDeleteContactViaApi() {
        logger.info("Creating and deleting contact via API");

        // Create first
        String randomEmail = "delete-api-" + UUID.randomUUID().toString().substring(0, 8) + "@automation.test";
        Map<String, Object> channels = new HashMap<>();
        Map<String, Object> emailChannel = new HashMap<>();
        emailChannel.put("address", randomEmail);
        emailChannel.put("subscribeStatus", "subscribed");
        channels.put("email", emailChannel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("channels", channels);

        Response createResponse = post("/contacts", payload);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        String contactId = createResponse.jsonPath().getString("_id");

        // Delete
        Response deleteResponse = delete("/contacts/" + contactId);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);

        // Verify deleted
        Response getResponse = get("/contacts/" + contactId);
        Assert.assertEquals(getResponse.getStatusCode(), 404);

        logger.info("Contact deleted successfully via API");
    }
}
