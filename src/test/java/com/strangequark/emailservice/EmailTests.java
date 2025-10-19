// Integration file: Email

package com.strangequark.emailservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ExtentTestWatcher.class)
public class EmailTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static EmailFunctions emailFunctions;
    private static AuthFunctions authFunctions; // Integration line: Auth

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
        emailFunctions = new EmailFunctions(apiRequestContext
            , authFunctions // Integration line: Auth
        );
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = emailFunctions.healthcheck();
        assertTrue(response.ok(), "Email service healthcheck failed: " + response.status() + " - " + response.text());
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
        assertTrue(response.ok(), "Confirm email token test failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Auth
    @Test
    public void enableUserTest() {
        APIResponse response = emailFunctions.enableUser();
        assertTrue(response.ok(), "Email enable user test failed: " + response.status() + " - " + response.text());

        // Cleanup the user that was created
        response = authFunctions.deleteUser(emailFunctions.testUsername, emailFunctions.testEmail, emailFunctions.testPassword);
        assertTrue(response.ok(), "Email enable user test cleanup failed: " + response.status() + " - " + response.text());
    }// Integration function end: Auth
}
