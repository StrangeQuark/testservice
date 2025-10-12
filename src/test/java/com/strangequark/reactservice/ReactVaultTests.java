// Integration file: React
// Integration file: Vault

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactVaultTests extends ReactTestsBase {
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
    @Test
    public void userCreateServiceTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.fillAndSubmitCreateServiceForm(page, serviceName);

        page.getByText(serviceName).waitFor(WAIT_FOR_ATTACHED);

        Locator serviceSelectOptions = page.locator("#service-select option");
        assertTrue(serviceSelectOptions.allInnerTexts().contains(serviceName),
                "Service name should be present in the dropdown after creation");
    }

    @Test
    public void userCreateEnvironmentTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.fillAndSubmitCreateServiceForm(page, serviceName);
        reactFunctions.selectService(page, serviceName);

        reactFunctions.fillAndSubmitCreateEnvironmentForm(page, environmentName);

        page.getByText(environmentName).waitFor(WAIT_FOR_ATTACHED);

        Locator environmentSelectOptions = page.locator("#environment-select option");
        assertTrue(environmentSelectOptions.allInnerTexts().contains(environmentName),
                "Environment name should be present in the dropdown after creation");
    }

    @Test
    public void userAddVariableTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.fillAndSubmitCreateServiceForm(page, serviceName);
        reactFunctions.selectService(page, serviceName);

        reactFunctions.fillAndSubmitCreateEnvironmentForm(page, environmentName);
        reactFunctions.selectEnvironment(page, environmentName);

        reactFunctions.fillAndSubmitAddVariableForm(page, testVariableKey, testVariableValue);

        Locator variableKeyInput = page.locator("input[value='" + testVariableKey + "']");

        variableKeyInput.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(variableKeyInput.isVisible(), "Variable key input should be visible after variable creation");
    }
}
