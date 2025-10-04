// Integration file: React

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.authservice.AuthFunctions;
import com.strangequark.utility.AuthUtility;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private AuthUtility authUtility;

    String username;
    String email;
    String password;// Integration function end: Auth

    @BeforeAll
    public void beforeAll() {
        playwright = Playwright.create();
        reactFunctions = new ReactFunctions();
        apiRequestContext = playwright.request().newContext(); // Integration function start: Auth
        authFunctions = new AuthFunctions(apiRequestContext);
        authUtility = new AuthUtility(apiRequestContext); // Integration function end: Auth
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
    // Integration function start: Auth
    @Test
    public void ensureLoginDivLoading() {
        reactFunctions.navigateToLogin(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(loginDiv.isVisible(), "Login div should be visible after navigating to login page");
    }

    @Test
    public void ensureRegisterDivLoading() {
        reactFunctions.navigateToRegister(page);

        Locator registerDiv = page.locator("id=register-div");

        registerDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(registerDiv.isVisible(), "Register div should be visible after navigating to register page");
    }

    @Test
    public void userRegisterTest() {
        reactFunctions.navigateToRegister(page);
        reactFunctions.fillAndSubmitRegisterForm(page, username, email, password);

        Locator requestSuccessTextField = page.locator("id=request-success-text-field");

        requestSuccessTextField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(requestSuccessTextField.isVisible(), "Request success div should be visible after navigating to register page");
    }

    @Test
    public void userLoginTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password);

        Locator usernameButton = page.getByText(username);

        usernameButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(usernameButton.isVisible(), "The logged in user's button should be visible after logging in");
    }
    // Integration function start: Email
    @Test
    public void ensurePasswordResetDivLoading() {
        reactFunctions.navigateToPasswordReset(page);

        Locator resetPasswordDiv = page.locator("id=request-div");

        resetPasswordDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(resetPasswordDiv.isVisible(), "Reset password div should be visible after navigating to password-reset page");
    }

    @Test
    public void ensureNewPasswordDivLoading() {
        reactFunctions.navigateToNewPassword(page);

        Locator newPasswordDiv = page.locator("id=request-div");

        newPasswordDiv.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(newPasswordDiv.isVisible(), "New password div should be visible after navigating to new-password page");
    }

    @Test
    public void userRegisterEmailTest() {
        reactFunctions.registerAndEnable(page, username, email, password);

        assertTrue(page.locator("id=message-div").isVisible(), "Success message div should be visible after clicking the register email link");
    }
    // Integration function end: Email
    // Integration function end: Auth
}
