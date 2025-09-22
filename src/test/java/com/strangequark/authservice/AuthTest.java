package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.AuthUtility;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private final AuthUtility authUtility = new AuthUtility();

    private String testUsername;
    private String testEmail;
    private String testPassword;

    private static final String BASE_URL = "http://localhost:6001/api/auth";

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        // Skip teardown for healthcheckTest
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

        enableUser(testEmail);// Integration line: Email
        APIResponse response = authenticate(testUsername, testPassword);
        response = serveAccessToken(extractJwt(response));
        response = deleteUser(testUsername, testPassword, extractJwt(response));
        if (!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        }
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = healthcheck();
        assertTrue(response.ok(), "Healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void enableUserTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
    }// Integration function start: end

    @Test
    public void authenticateTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serveAccessTokenTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getUserIdTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void searchUsersByUsernameTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users by username failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void searchUsersByEmailTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = searchUsers(testEmail, accessToken);
        assertTrue(response.ok(), "Search users by email failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void getUserDetailsByIdsTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());

        List<String> ids = new ArrayList<>();
        ids.add(response.text().replace("\"", ""));

        response = getUserDetailsByIds(ids, accessToken);
        assertTrue(response.ok(), "Get user details by ids failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void disableUserByUsernameTest() {
        String accessToken = registerEnableAuthenticateAccess();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", testUsername);

        APIResponse response = disableUser(requestBody, accessToken);
        assertTrue(response.ok(), "Disable user failed: " + response.status() + " - " + response.text());

        response = authenticate(testUsername, testPassword);
        assertTrue(!response.ok(), "Disable user failed - User still enabled: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("User is disabled", jsonObject.get("errorMessage").getAsString());
    }

    private String registerEnableAuthenticateAccess() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        return extractJwt(response);
    }

    private APIResponse healthcheck() {
        return apiRequestContext.get(BASE_URL + "/health");
    }

    private APIResponse register(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/register", RequestOptions.create().setData(requestBody));
    }
    // Integration function start: Email
    private APIResponse enableUser(String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(BASE_URL + "/user/enable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()));
    }// Integration function end: Email

    private APIResponse disableUser(Map<String, String> requestBody, String accessToken) {
        return apiRequestContext.post(BASE_URL + "/user/disable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse authenticate(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/authenticate", RequestOptions.create().setData(requestBody));
    }

    private APIResponse serveAccessToken(String refreshToken) {
        return apiRequestContext.get(BASE_URL + "/access", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + refreshToken));
    }

    private APIResponse getUserId(String username, String accessToken) {
        return apiRequestContext.get(BASE_URL + "/user/get-user-id?username=" + username, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse searchUsers(String query, String accessToken) {
        return apiRequestContext.get(BASE_URL + "/user/search-users?query=" + query, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse getUserDetailsByIds(List<String> ids, String accessToken) {
        return apiRequestContext.post(BASE_URL + "/user/get-user-details-by-ids", RequestOptions.create()
                .setData(ids)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse deleteUser(String username, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/user/delete-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private String extractJwt(APIResponse response) {
        if (response.ok()) {
            JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
            return jsonObject.get("jwtToken").getAsString();
        } else {
            throw new RuntimeException("Unable to extract JWT from failed response: " + response.status());
        }
    }
}
