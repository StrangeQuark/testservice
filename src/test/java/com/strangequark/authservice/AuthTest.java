package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.AuthUtility;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private APIResponse deleteUser(String username, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        System.out.println(accessToken);

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
