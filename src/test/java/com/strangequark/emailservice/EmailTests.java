// Integration file: Email

package com.strangequark.emailservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static EmailFunctions emailFunctions;
    private static AuthFunctions authFunctions; // Integration function start: Auth

    public static String testUsername;
    public static String testEmail;
    public static String testPassword;// Integration function end: Auth

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
        emailFunctions = new EmailFunctions(apiRequestContext
            , authFunctions // Integration line: Auth
        );
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }
        // Integration function start: Auth
        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString(); // Integration function end: Auth
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }
        // Integration function start: Auth
        authFunctions.enableUser(testEmail);
        APIResponse response = authFunctions.authenticate(testUsername, testPassword);
        response = authFunctions.serveAccessToken(authFunctions.extractJwt(response));
        response = authFunctions.deleteUser(testUsername, testPassword, authFunctions.extractJwt(response));
        if (!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        } // Integration function end: Auth
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = emailFunctions.healthcheck();
        assertTrue(response.ok(), "Healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void sendEmailTest() {
        APIResponse response = emailFunctions.sendEmail("recipient@email.com", "sender@email.com",
                "Test email", "Test subject");

        assertTrue(response.ok(), "Send email test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void sendEmailWithTokenTest() {
        APIResponse response = emailFunctions.sendEmailWithToken("recipient@email.com", "sender@email.com",
                "Test email", "Test subject");
        assertTrue(response.ok(), "Send email with token test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void sendRegisterEmailTest() {
        APIResponse response = emailFunctions.sendRegisterEmail("recipient@email.com", "sender@email.com", "Test subject");
        assertTrue(response.ok(), "Send register email test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void sendPasswordResetEmailTest() {
        APIResponse response = emailFunctions.sendPasswordResetEmail("recipient@email.com", "sender@email.com", "Test subject");
        assertTrue(response.ok(), "Send password reset email test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void confirmTokenTest() {
        APIResponse response = emailFunctions.confirmToken();
        assertTrue(response.ok(), "Confirm Email token test failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Auth
    @Test
    public void enableUserTest() {
        APIResponse response = emailFunctions.enableUser();
        assertTrue(response.ok(), "Email enable user test failed: " + response.status() + " - " + response.text());
    }// Integration function end: Auth
}
