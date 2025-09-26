package com.strangequark.fileservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static FileFunctions fileFunctions;
    private static AuthFunctions authFunctions; // Integration line: Auth

    private String testCollectionName;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
        fileFunctions = new FileFunctions(apiRequestContext
                , authFunctions // Integration line: Auth
        );
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("createCollectionTest")) {
            return;
        }
        testCollectionName = "testCollection_" + UUID.randomUUID();
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

//        APIResponse response = fileFunctions.deleteCollection(testCollectionName);
//        if (!response.ok()) {
//            System.err.println("Cleanup failed for " + testCollectionName + ": " + response.status() + " - " + response.text());
//        }
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = fileFunctions.healthcheck();
        assertTrue(response.ok(), "File service healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void createCollectionTest() {
        APIResponse response = fileFunctions.createCollection(testCollectionName);
        assertTrue(response.ok(), "Create collection test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void uploadTest() {
        APIResponse response = fileFunctions.createCollection(testCollectionName);
        assertTrue(response.ok(), "Create collection failed: " + response.status() + " - " + response.text());

        response = fileFunctions.upload(testCollectionName);
        assertTrue(response.ok(), "File upload test failed: " + response.status() + " - " + response.text());
    }
}
