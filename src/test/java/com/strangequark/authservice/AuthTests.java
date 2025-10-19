// Integration file: Auth

package com.strangequark.authservice;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static AuthFunctions authFunctions;

    private String testUsername;
    private String testEmail;
    private String testPassword;

    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext);

        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("test-results/auth-report.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();

        test = extent.createTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("deleteUserTest")  ||
                testInfo.getTestMethod().get().getName().equals("serviceAccountAuthenticationTest")) {
            return;
        }

        APIResponse response = authFunctions.deleteUser(testUsername, testEmail, testPassword);
        if(!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        }
    }

    @AfterAll
    static void afterAll() {
        extent.flush();
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = authFunctions.healthcheck();
        assertTrue(response.ok(), "Auth service healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void enableUserTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
    }// Integration function end: Email

    @Test
    public void authenticateTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serveAccessTokenTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken(authFunctions.extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getUserIdTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void searchUsersByUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users by username failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void searchUsersByEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.searchUsers(testEmail, accessToken);
        assertTrue(response.ok(), "Search users by email failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void getUserDetailsByIdsTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());

        List<String> ids = new ArrayList<>();
        ids.add(response.text().replace("\"", ""));

        response = authFunctions.getUserDetailsByIds(ids, accessToken);
        assertTrue(response.ok(), "Get user details by ids failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void disableUserByUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.disableUser(testUsername, accessToken);
        assertTrue(response.ok(), "Disable user failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Disable user failed - User still enabled: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("User is disabled", jsonObject.get("errorMessage").getAsString());
    }

    @Test
    public void updatePasswordTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        String newPassword = "newTestPassword";

        APIResponse response = authFunctions.updatePassword(testPassword, newPassword, accessToken);
        assertTrue(response.ok(), "Update password failed: " + response.status() + " - " + response.text());

        // Set the testPassword to the newPassword for afterEach method
        testPassword = newPassword;
        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Update password login attempt failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void addAuthorizationsToUserTest() {
        APIResponse response = authFunctions.bootstrapSuperUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Bootstrap super user failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken(authFunctions.extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        String accessToken = authFunctions.extractJwt(response);
        List<String> auths = new ArrayList<>();
        auths.add("Auth 1");
        auths.add("Auth 2");

        response = authFunctions.addAuthorizationsToUser(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Add authorizations test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("Authorizations successfully added", jsonObject.get("message").getAsString());
    }

    @Test
    public void removeAuthorizationsTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        List<String> auths = new ArrayList<>();
        auths.add("Auth 1");

        APIResponse response = authFunctions.removeAuthorizations(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Remove authorizations test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void deleteUserTest() {
        authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.deleteUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Delete user test failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Delete user test failed - Able to authenticate: " + response.status() + " - " + response.text());
    }

    @Test
    public void updateEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        testEmail = "new@email.com";

        APIResponse response = authFunctions.updateEmail(testEmail, testPassword, accessToken);
        assertTrue(response.ok(), "Update email failed: " + response.status() + " - " + response.text());

        response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void updateUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        testUsername = "newUsername";

        APIResponse response = authFunctions.updateUsername(testUsername, testPassword, accessToken);
        assertTrue(response.ok(), "Update username failed: " + response.status() + " - " + response.text());

        // We have to re-fetch the accessToken since the user's username has changed
        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
        response = authFunctions.serveAccessToken(authFunctions.extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        accessToken = authFunctions.extractJwt(response);

        response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
    }

    @Test
    public void updateRoleTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.updateRole("SUPER", testUsername,  accessToken);
        assertFalse(response.ok(), "Update role failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void sendPasswordResetEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.sendPasswordResetEmail(testEmail, accessToken);
        assertTrue(response.ok(), "Send password reset email failed: " + response.status() + " - " + response.text());
    } // Integration function end: Email

    @Test
    public void bootstrapSuperUserTest() {
        APIResponse response = authFunctions.bootstrapSuperUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Bootstrap super user failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serviceAccountAuthenticationTest() {
        APIResponse response = authFunctions.serviceAccountAuthenticate("test");
        assertTrue(response.ok(), "Service account authentication failed: " + response.status() + " - " + response.text());
    }
}
