// Integration file: React

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.authservice.AuthFunctions;
import com.strangequark.utility.AuthUtility;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReactTests {
    private Playwright playwright;
    private Page page;
    private Browser browser;
    private ReactFunctions reactFunctions;
    private static APIRequestContext apiRequestContext; // Integration function start: Auth
    private AuthFunctions authFunctions;
    private AuthUtility authUtility; // Integration function end: Auth

    @BeforeAll
    public void beforeAll() {
        playwright = Playwright.create();
        reactFunctions = new ReactFunctions();
        apiRequestContext = playwright.request().newContext(); // Integration function start: Auth
        authFunctions = new AuthFunctions(apiRequestContext);
        authUtility = new AuthUtility(apiRequestContext); // Integration function end: Auth
    }

    @BeforeEach
    public void beforeEach() {
        browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @AfterEach
    public void afterEach() {
        browser.close();
        page.close();
    }
    // Integration function start: Auth
    @Test
    public void ensureLoginDivLoading() {
        reactFunctions.navigateToLogin(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(loginDiv.isVisible(), "Login div should be visible after clicking loginButton");
    }

    @Test
    public void ensureRegisterDivLoading() {
        reactFunctions.navigateToRegister(page);

        Locator registerDiv = page.locator("id=register-div");

        registerDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(registerDiv.isVisible(), "Login div should be visible after clicking loginButton");
    }

    @Test
    public void registerTest() {
        String username = "test_" + UUID.randomUUID();
        String email = username + "@testEmail.com";
        String password = "testPassword123!";

        reactFunctions.navigateToRegister(page);
        reactFunctions.fillRegisterForm(page, username, email, password);

        Locator requestSuccessTextField = page.locator("id=request-success-text-field");

        requestSuccessTextField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(requestSuccessTextField.isVisible(), "Login div should be visible after clicking loginButton");

        // Cleanup and ensure user was deleted
        authFunctions.enableUser(email);
        authFunctions.deleteUser(username, password);
        assertFalse(authFunctions.getUserId(username, password).ok(), "User cleanup failed in React service register test");
    }// Integration function end: Auth
}
