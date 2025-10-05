// Integration file: React
// Integration file: File

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactFileTests extends ReactTestsBase {
    @Test
    public void ensureToolbarFilesButtonRedirectsToLogin() {
        reactFunctions.navigateToLogin(page);

        reactFunctions.clickToolbarFilesButton(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(loginDiv.isVisible(), "Login div should be visible after navigating to login page");
    }
}
