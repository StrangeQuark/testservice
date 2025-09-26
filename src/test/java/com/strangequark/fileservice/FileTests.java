package com.strangequark.fileservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions;
import com.strangequark.emailservice.EmailFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static FileFunctions fileFunctions;
    private static AuthFunctions authFunctions; // Integration line: Auth

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
        fileFunctions = new FileFunctions(apiRequestContext
                , authFunctions // Integration line: Auth
        );
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = fileFunctions.healthcheck();
        assertTrue(response.ok(), "Healthcheck failed: " + response.status() + " - " + response.text());
    }
}
