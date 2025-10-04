package com.strangequark.reactservice;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReactFunctions {
    public ReactFunctions() {

    }
    public void navigateToHomePage(Page page) {
        page.navigate("localhost:6080/");
    }
    // Integration function start: Auth
    public void navigateToLogin(Page page) {
        page.navigate("localhost:6080/login");
    }

    public void navigateToRegister(Page page) {
        page.navigate("localhost:6080/register");
    }

    public void fillAndSubmitRegisterForm(Page page, String username, String email, String password) {
        page.locator("id=username").fill(username);
        page.locator("id=email").fill(email);
        page.locator("id=password").fill(password);
        page.locator("id=confirm-password").fill(password);

        page.click("id=submit-button");
    }

    public void fillAndSubmitLoginForm(Page page, String username, String password) {
        page.locator("id=username").fill(username);
        page.locator("id=password").fill(password);

        page.click("id=submit-button");
    }

    public void registerAndEnable(Page page, String username, String email, String password) {
        navigateToRegister(page);
        fillAndSubmitRegisterForm(page, username, email, password);
        // We must wait for the success div otherwise we sometimes navigateToMailbox too quickly and don't send the requests
        page.locator("id=request-success-text-field")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        // Integration function start: Email
        navigateToMailbox(page);
        page.getByText(email).click();

        FrameLocator emailFrame = page.frameLocator("iframe").first();

        Locator confirmLink = emailFrame.getByText("confirm-email?token=");
        confirmLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String confirmUrl = confirmLink.getAttribute("href");
        assertNotNull(confirmUrl, "Confirmation link should have an href attribute");

        page.navigate(confirmUrl);
        page.locator("id=message-div").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        // Integration function end: Email
    }

    public void registerEnableAndLogin(Page page, String username, String email, String password) {
        registerAndEnable(page, username, email, password);

        navigateToLogin(page);

        fillAndSubmitLoginForm(page, username, password);
    }
    // Integration function end: Auth
    // Integration function start: Email
    public void navigateToPasswordReset(Page page) {
        page.navigate("localhost:6080/password-reset");
    }

    public void navigateToNewPassword(Page page) {
        page.navigate("localhost:6080/new-password");
    }

    public void navigateToMailbox(Page page) {
        page.navigate("localhost:1080");
    }
    // Integration function end: Email
    // Integration function start: File
    public void clickToolbarFilesButton(Page page) {
        page.getByText("Files").click();
    }
    // Integration function end: File
    // Integration function start: Vault
    public void clickToolbarVaultButton(Page page) {
        page.getByText("Vault").click();
    }
    // Integration function end: Vault
}
