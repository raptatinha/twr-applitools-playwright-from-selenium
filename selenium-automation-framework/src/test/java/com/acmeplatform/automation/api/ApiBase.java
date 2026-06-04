package com.acmeplatform.automation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * ApiBase - Base class for REST API testing with RestAssured.
 * 
 * ============================================================================================
 * THE ISOLATION PROBLEM:
 *
 * In Selenium + RestAssured, API tests are a COMPLETELY SEPARATE WORLD:
 *   - Different auth mechanism (API key vs browser session)
 *   - Different base URL configuration
 *   - Cannot share cookies between UI tests and API calls
 *   - Cannot mix UI + API actions in the same test
 *   - Separate test classes, separate setup, separate teardown
 *
 * In Playwright, the `request` fixture shares the browser context:
 *   test('create via API and verify in UI', async ({ page, request }) => {
 *     // API call uses the SAME auth session as the browser
 *     const response = await request.post('/api/contacts', { data: payload });
 *     // Navigate in the SAME test to verify
 *     await page.goto('/contacts/' + response.json()._id);
 *     await expect(page.getByTestId('email')).toHaveText(email);
 *   });
 *
 * This means:
 *   - No separate auth setup
 *   - No RestAssured dependency
 *   - API and UI in the same test, same session
 *   - Pre-conditions created via API, verified via UI (or vice versa)
 *   - One framework, one language, one auth mechanism
 *
 * With Selenium you need: Selenium + TestNG + RestAssured + separate config.
 * With Playwright you need: Playwright. That's it.
 * ============================================================================================
 *
 * Pain points demonstrated:
 * - Completely separate from UI test layer (no shared session/cookies)
 * - Must manage its own authentication (can't reuse browser session)
 * - Duplicate configuration (base URL, credentials defined separately)
 * - No integration with Selenium WebDriver context
 * - If a test needs both API and UI, you need two separate auth mechanisms
 */
public class ApiBase {

    protected static final Logger logger = LogManager.getLogger(ApiBase.class);
    protected static String apiBaseUrl;
    protected static String apiKey;
    protected static String accountId;

    // Separate authentication - cannot share with Selenium browser session
    protected static String authToken;

    public static void setupApi(String baseUrl, String key, String account) {
        apiBaseUrl = baseUrl;
        apiKey = key;
        accountId = account;

        RestAssured.baseURI = apiBaseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        logger.info("API Base URL: " + apiBaseUrl);
        logger.info("Account ID: " + accountId);
    }

    /**
     * Must authenticate separately from the UI session.
     * In Playwright, the request fixture shares the browser context cookies.
     */
    public static void authenticate() {
        logger.info("Authenticating API separately (not shared with UI session)");

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("X-API-Key", apiKey)
                .body("{\"accountId\": \"" + accountId + "\"}")
                .post("/auth/token");

        if (response.getStatusCode() == 200) {
            authToken = response.jsonPath().getString("token");
            logger.info("API authentication successful");
        } else {
            logger.error("API authentication failed: " + response.getStatusCode());
            throw new RuntimeException("API auth failed: " + response.getBody().asString());
        }
    }

    protected RequestSpecification getAuthenticatedRequest() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .header("X-Account-ID", accountId);
    }

    protected Response get(String endpoint) {
        logger.info("GET " + endpoint);
        return getAuthenticatedRequest().get(endpoint);
    }

    protected Response post(String endpoint, Object body) {
        logger.info("POST " + endpoint);
        return getAuthenticatedRequest().body(body).post(endpoint);
    }

    protected Response post(String endpoint, Map<String, Object> body) {
        logger.info("POST " + endpoint);
        return getAuthenticatedRequest().body(body).post(endpoint);
    }

    protected Response put(String endpoint, Object body) {
        logger.info("PUT " + endpoint);
        return getAuthenticatedRequest().body(body).put(endpoint);
    }

    protected Response delete(String endpoint) {
        logger.info("DELETE " + endpoint);
        return getAuthenticatedRequest().delete(endpoint);
    }
}
