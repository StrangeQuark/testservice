// Integration file: React
// Integration file: Vault

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactVaultTests extends ReactTestsBase{
    // Integration function start: Auth
    @Test
    public void ensureToolbarVaultButtonRedirectsToLoginTest() {
        reactFunctions.navigateToLogin(page);

        reactFunctions.clickToolbarVaultButton(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(loginDiv.isVisible(), "Login div should be visible after clicking the Vault button on the " +
                "toolbar without logging in");
    }
    // Integration function end: Auth
}
