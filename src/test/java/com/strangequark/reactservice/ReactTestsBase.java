// Integration file: React

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.authservice.AuthFunctions;
import org.junit.jupiter.api.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReactTestsBase {
    public Playwright playwright;
    public Page page;
    public Browser browser;
    public ReactFunctions reactFunctions;
    public final Locator.WaitForOptions WAIT_FOR_VISIBLE = new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    public final Locator.WaitForOptions WAIT_FOR_DETACHED = new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED);
    public static APIRequestContext apiRequestContext; // Integration function start: Auth
    public AuthFunctions authFunctions;

    public String username;
    public String email;
    public String password;// Integration function end: Auth
    public String collectionName;// Integration function start: File
    public String textFileName;
    public Path textFilePath;// Integration function end: File

    @BeforeAll
    public void beforeAll() throws URISyntaxException {
        playwright = Playwright.create();
        reactFunctions = new ReactFunctions();
        apiRequestContext = playwright.request().newContext(); // Integration line: Auth
        authFunctions = new AuthFunctions(apiRequestContext);// Integration line: Auth
        textFileName = "testUploadFile.txt";// Integration line: File
        textFilePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + textFileName).toURI());// Integration line: File
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        // Integration function start: Auth
        if(testInfo.getTestMethod().get().getName().startsWith("user")) {
            username = "test_" + UUID.randomUUID();
            email = username + "@testEmail.com";
            password = "testPassword123!";
        }// Integration function end: Auth
        collectionName = "collection_" + UUID.randomUUID();// Integration line: File
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        browser.close();
        page.close();
        // Integration function start: Auth
        if(testInfo.getTestMethod().get().getName().startsWith("user")) {
            authFunctions.deleteUser(username, email, password);
            assertFalse(authFunctions.getUserId(username, password).ok(), "User cleanup failed in React service register test");
        }// Integration function end: Auth
    }
}
