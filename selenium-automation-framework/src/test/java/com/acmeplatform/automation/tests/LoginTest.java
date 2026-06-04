package com.acmeplatform.automation.tests;

import com.acmeplatform.automation.base.BaseTest;
import com.acmeplatform.automation.pages.DashboardPage;
import com.acmeplatform.automation.pages.LoginPage;
import com.acmeplatform.automation.utils.ConfigReader;
import com.acmeplatform.automation.utils.ReportManager;
import com.acmeplatform.automation.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * LoginTest - Authentication test cases.
 * 
 * Pain points demonstrated:
 * - Thread.sleep between steps where Playwright auto-waits
 * - Manual page object instantiation (no fixtures)
 * - Verbose assertions without auto-retrying
 * - Must manually verify page transitions
 */
public class LoginTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @BeforeMethod
    public void initPages() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
    }

    @Test(description = "Verify successful login with valid credentials and logout")
    public void testLoginWithValidCredentials() {
        ReportManager.logStep("Starting login test with valid credentials");

        // Navigate to login page
        loginPage.verifyLoginPageLoaded();

        // Enter credentials
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);

        // Click login
        loginPage.clickLoginButton();

        // Wait for dashboard - explicit wait needed because no auto-waiting
        WaitUtils.hardWait(3000);
        WaitUtils.waitForPageLoad(driver);

        // Verify dashboard loaded
        dashboardPage.verifyDashboardLoaded();
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "Dashboard should be displayed after successful login");

        ReportManager.logStep("Login successful, verifying dashboard");

        // Now test logout
        WaitUtils.hardWait(1000);
        dashboardPage.logout();

        // Verify redirected back to login
        WaitUtils.hardWait(2000);
        loginPage.verifyLoginPageLoaded();

        ReportManager.logStep("Logout successful, login page displayed");
    }

    @Test(description = "Verify login fails with invalid credentials")
    public void testLoginWithInvalidCredentials() {
        ReportManager.logStep("Starting invalid login test");

        loginPage.verifyLoginPageLoaded();

        // Enter invalid credentials
        loginPage.enterEmail("invalid@test.com");
        loginPage.enterPassword("wrongpassword123");
        loginPage.clickLoginButton();

        // Wait for error message to appear
        WaitUtils.hardWait(2000);

        // Verify error is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                "Error message should be displayed for invalid credentials");

        String errorMsg = loginPage.getErrorMessage();
        Assert.assertTrue(errorMsg.contains("Invalid") || errorMsg.contains("incorrect"),
                "Error message should indicate invalid credentials. Actual: " + errorMsg);

        ReportManager.logStep("Invalid login error displayed: " + errorMsg);
    }

    @Test(description = "Verify email and password fields show validation errors when empty")
    public void testLoginFieldValidation() {
        ReportManager.logStep("Starting field validation test");

        loginPage.verifyLoginPageLoaded();

        // Click login without entering anything
        loginPage.clickLoginButton();

        // Wait for validation errors
        WaitUtils.hardWait(1500);

        // Check validation errors
        Assert.assertTrue(loginPage.isEmailErrorDisplayed(),
                "Email validation error should be displayed");
        Assert.assertTrue(loginPage.isPasswordErrorDisplayed(),
                "Password validation error should be displayed");

        ReportManager.logStep("Field validation errors displayed correctly");
    }
}
