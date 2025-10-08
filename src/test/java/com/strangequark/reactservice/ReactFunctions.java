// Integration file: React

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

        // We must wait for the success div to ensure request sent
        page.locator("id=request-success-text-field")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillAndSubmitLoginForm(Page page, String username, String password) {
        page.locator("id=username").fill(username);
        page.locator("id=password").fill(password);

        page.click("id=submit-button");

        page.waitForURL("http://localhost:6080/");
    }

    public void fillAndSubmitPasswordResetForm(Page page, String username) {
        page.locator("id=credentials").fill(username);

        page.click("id=submit-button");

        // We must wait for the success div to ensure request sent
        page.locator("id=request-success-div")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillAndSubmitNewPasswordForm(Page page, String newPassword) {
        page.locator("id=password").fill(newPassword);
        page.locator("id=confirm-password").fill(newPassword);

        page.click("id=submit-button");

        // We must wait for the success div to ensure request sent
        page.locator("id=request-success-div")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void registerAndEnable(Page page, String username, String email, String password) {
        navigateToRegister(page);
        fillAndSubmitRegisterForm(page, username, email, password);
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

    public void navigateToUserSettings(Page page) {
        page.navigate("localhost:6080/settings");
    }

    public void clickEditAndSubmitUpdateUsername(Page page, String username, String password) {
        page.getByTestId("update-username").click();

        page.locator("id=input-newUsername-0").fill(username);
        page.locator("id=input-password-1").fill(password);

        page.getByText("Save").click();
    }

    public void clickEditAndSubmitUpdateEmail(Page page, String email, String password) {
        page.getByTestId("update-email").click();

        page.locator("id=input-newEmail-0").fill(email);
        page.locator("id=input-password-1").fill(password);

        page.getByText("Save").click();
    }

    public void clickEditAndSubmitUpdatePassword(Page page, String password, String newPassword) {
        page.getByTestId("update-password").click();

        page.locator("id=input-password-0").fill(password);
        page.locator("id=input-newPassword-1").fill(newPassword);

        page.getByText("Save").click();
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

    public void clickPasswordResetEmail(Page page, String email) {
        Locator subline = page.locator("span.title-subline.ng-binding")
                .filter(new Locator.FilterOptions().setHasText(email));

        Locator parent = subline.locator("..")
                .filter(new Locator.FilterOptions().setHasText("Password reset"));

        parent.click(new Locator.ClickOptions().setTimeout(10000));

        FrameLocator emailFrame = page.frameLocator("iframe").first();

        Locator resetLink = emailFrame.getByText("Reset password");
        resetLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String resetUrl = resetLink.getAttribute("href");
        assertNotNull(resetUrl, "Reset link should have an href attribute");

        page.navigate(resetUrl);
        page.locator("id=request-div").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
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
