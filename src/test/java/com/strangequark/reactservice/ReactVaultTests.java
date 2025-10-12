// Integration file: React
// Integration file: Vault

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);

        Locator variableKeyInput = page.locator("id=key-" + testVariableKey);
        assertTrue(variableKeyInput.isVisible(), "Variable key input should be visible after variable creation");
    }

    @Test
    public void userUnmaskVariableTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);
        reactFunctions.unmaskVariable(page, testVariableKey);

        Locator variableValueInput = page.locator("id=value-" + testVariableKey);

        variableValueInput.waitFor(WAIT_FOR_VISIBLE);
        assertFalse(variableValueInput.getAttribute("class").contains("masked-input"), "Variable value should be visible after unmasking");
    }

    @Test
    public void userCopyVariableTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);

        Locator variableCopyButton = page.locator("id=copy-" + testVariableKey);

        // Spy on clipboard to avoid permission issues
        page.evaluate("""
            () => { 
                window.__copiedText = ''; 
                navigator.clipboard.writeText = text => { window.__copiedText = text; return Promise.resolve(); }; 
            }
        """);

        variableCopyButton.click();

        assertEquals(testVariableValue, page.evaluate("() => window.__copiedText"),
                "Clipboard text should match the test variable's value after copying");
    }

    @Test
    public void userDeleteVariableTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);

        reactFunctions.handleNextAlert(page, true);
        reactFunctions.deleteVariable(page, testVariableKey);

        Locator variableKeyInput = page.locator("id=key-" + testVariableKey);

        variableKeyInput.waitFor(WAIT_FOR_DETACHED);
        assertFalse(variableKeyInput.isVisible(), "Variable key input should not be visible after variable deletion");
    }

    @Test
    public void userUploadEnvFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);

        reactFunctions.uploadEnvFile(page, envFilePath);

        Locator variableKeyInput = page.locator("id=key-testEnvKey");

        variableKeyInput.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(variableKeyInput.isVisible(), "Variable key input should be visible after uploading env file");
    }

    @Test
    public void userDownloadEnvFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToVault(page);

        reactFunctions.createServiceEnvironmentAndVariable(page, serviceName, environmentName, testVariableKey, testVariableValue);

        Download download = reactFunctions.clickEnvDownloadButton(page);

        assertEquals(serviceName + "." + environmentName + ".env", download.suggestedFilename(), "Download file name should match textFileName");

        String fileContent = reactFunctions.getEnvFileContent(download.path());

        String expectedContent = testVariableKey + "=" + testVariableValue + "\n";
        assertEquals(expectedContent, fileContent, "Downloaded env file contents should match expected");
    }
}
