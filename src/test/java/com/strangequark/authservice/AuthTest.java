package com.strangequark.authservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;

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
    public void beforeEach() {
        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();
    }

    @AfterEach
    public void afterEach() {
        APIResponse response = deleteUser(testUsername, testPassword);
        if (!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        }
    }

    @Test
    public void registerTest() {
        APIResponse response = registerUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerAndEnableUser() {
        APIResponse response = registerUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerEnableAndAuthenticateUser() {
        APIResponse response = registerUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());

        response = authenticateUser(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
    }

    private APIResponse registerUser(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/register", RequestOptions.create().setData(requestBody));
    }

    private APIResponse enableUser(String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(BASE_URL + "/user/enable-user", RequestOptions.create().setData(requestBody));
    }

    private APIResponse authenticateUser(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/authenticate", RequestOptions.create().setData(requestBody));
    }

    private APIResponse deleteUser(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/user/delete-user", RequestOptions.create().setData(requestBody));
    }
}
