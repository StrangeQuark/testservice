package com.strangequark.fileservice;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static FileFunctions fileFunctions;
    private static AuthFunctions authFunctions; // Integration line: Auth

    private String testCollectionName;

    private final String TEXT_TEST_FILE = "testUploadFile.txt";

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

        APIResponse response = fileFunctions.createCollection(testCollectionName);
        assertTrue(response.ok(), "Create collection test failed: " + response.status() + " - " + response.text());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("deleteCollectionTest")) {
            return;
        }

        APIResponse response = fileFunctions.deleteCollection(testCollectionName);
        if (!response.ok()) {
            System.err.println("Cleanup failed for " + testCollectionName + ": " + response.status() + " - " + response.text());
        }
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
    public void getAllCollectionsTest() {
        APIResponse response = fileFunctions.getAllCollections();
        assertTrue(response.ok(), "Get all collections test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size());

        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        assertEquals(testCollectionName, jsonObject.get("name").getAsString());
    }

    @Test
    public void deleteCollectionTest() {
        APIResponse response = fileFunctions.deleteCollection(testCollectionName);
        assertTrue(response.ok(), "Create collection step in delete collection test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void uploadTest() {
        APIResponse response = fileFunctions.upload(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File upload test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void deleteTest() {
        APIResponse response = fileFunctions.upload(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File upload step in delete file test failed: " + response.status() + " - " + response.text());

        response = fileFunctions.delete(testCollectionName);
        assertTrue(response.ok(), "File delete test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getAllFilesTest() {
        APIResponse response = fileFunctions.upload(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File upload step in get all files test failed: " + response.status() + " - " + response.text());

        response = fileFunctions.getAllFiles(testCollectionName);
        assertTrue(response.ok(), "Get all files test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get all files return size test failed: " + response.status() + " - " + response.text());

        String returnFileName = jsonArray.get(0).getAsString();
        assertEquals(TEXT_TEST_FILE, returnFileName, "Get all files return file name test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void downloadFileTest() {
        APIResponse response = fileFunctions.upload(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File upload step in download file test failed: " + response.status() + " - " + response.text());

        response = fileFunctions.downloadFile(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File download test failed: " + response.status() + " - " + response.text());

        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + TEXT_TEST_FILE).toURI());
            assertEquals(Files.readString(filePath), response.text(), "Download file contents test failed: " + response.status() + " - " + response.text());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load test file from resources", ex);
        }
    }

    @Test
    public void streamFileTest() {
        APIResponse response = fileFunctions.upload(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File upload step in stream file test failed: " + response.status() + " - " + response.text());

        response = fileFunctions.streamFile(testCollectionName, TEXT_TEST_FILE);
        assertTrue(response.ok(), "File stream test failed: " + response.status() + " - " + response.text());

        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + TEXT_TEST_FILE).toURI());
            assertEquals(Files.readString(filePath), response.text(), "Stream file contents test failed: " + response.status() + " - " + response.text());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load test file from resources", ex);
        }
    }
}
