// Integration file: React
// Integration file: Auth

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactAuthTests extends ReactTestsBase {
    @Test
    public void ensureLoginDivLoading() {
        reactFunctions.navigateToLogin(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(loginDiv.isVisible(), "Login div should be visible after navigating to login page");
    }

    @Test
    public void ensureRegisterDivLoading() {
        reactFunctions.navigateToRegister(page);

        Locator registerDiv = page.locator("id=register-div");

        registerDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(registerDiv.isVisible(), "Register div should be visible after navigating to register page");
    }

    @Test
    public void userRegisterTest() {
        reactFunctions.navigateToRegister(page);
        reactFunctions.fillAndSubmitRegisterForm(page, username, email, password);

        Locator requestSuccessTextField = page.locator("id=request-success-text-field");

        requestSuccessTextField.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(requestSuccessTextField.isVisible(), "Request success div should be visible after navigating to register page");
    }

    @Test
    public void userLoginTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password);

        Locator usernameButton = page.getByText(username);

        usernameButton.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(usernameButton.isVisible(), "The logged in user's button should be visible after logging in");
    }
    // Integration function start: Email
    @Test
    public void ensurePasswordResetDivLoading() {
        reactFunctions.navigateToPasswordReset(page);

        Locator resetPasswordDiv = page.locator("id=request-div");

        resetPasswordDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(resetPasswordDiv.isVisible(), "Reset password div should be visible after navigating to password-reset page");
    }

    @Test
    public void ensureNewPasswordDivLoading() {
        reactFunctions.navigateToNewPassword(page);

        Locator newPasswordDiv = page.locator("id=request-div");

        newPasswordDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(newPasswordDiv.isVisible(), "New password div should be visible after navigating to new-password page");
    }

    @Test
    public void userRegisterEmailTest() {
        reactFunctions.registerAndEnable(page, username, email, password);

        Locator messageDiv = page.locator("id=message-div");

        messageDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(messageDiv.isVisible(), "Success message div should be visible after clicking the register email link");
    }
    // Integration function end: Email
}
