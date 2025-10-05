// Integration file: React

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.authservice.AuthFunctions;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReactTestsBase {
    public Playwright playwright;
    public Page page;
    public Browser browser;
    public ReactFunctions reactFunctions;
    public final Locator.WaitForOptions WAIT_FOR_VISIBLE = new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    public static APIRequestContext apiRequestContext; // Integration function start: Auth
    public AuthFunctions authFunctions;

    String username;
    String email;
    String password;// Integration function end: Auth

    @BeforeAll
    public void beforeAll() {
        playwright = Playwright.create();
        reactFunctions = new ReactFunctions();
        apiRequestContext = playwright.request().newContext(); // Integration line: Auth
        authFunctions = new AuthFunctions(apiRequestContext);// Integration line: Auth
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
