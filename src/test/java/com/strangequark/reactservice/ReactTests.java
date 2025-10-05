// Integration file: React

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactTests extends ReactTestsBase {
    @Test
    public void ensureHomePageLoading() {
        reactFunctions.navigateToHomePage(page);

        Locator homeButton = page.getByText("Home");

        homeButton.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(homeButton.isVisible(), "Home button should be visible in the toolbar after navigating to home page");
    }
}
