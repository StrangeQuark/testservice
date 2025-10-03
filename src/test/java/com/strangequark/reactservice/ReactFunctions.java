package com.strangequark.reactservice;

import com.microsoft.playwright.Page;

public class ReactFunctions {
    public ReactFunctions() {

    }
    // Integration function start: Auth
    public void navigateToLogin(Page page) {
        page.navigate("localhost:6080/login");
    }

    public void navigateToRegister(Page page) {
        page.navigate("localhost:6080/register");
    }

    public void fillRegisterForm(Page page, String username, String email, String password) {
        page.locator("id=username").fill(username);
        page.locator("id=email").fill(email);
        page.locator("id=password").fill(password);
        page.locator("id=confirm-password").fill(password);

        page.click("id=submit-button");
    }// Integration function end: Auth
    // Integration function start: Email
    public void navigateToPasswordReset(Page page) {
        page.navigate("localhost:6080/password-reset");
    }

    public void navigateToNewPassword(Page page) {
        page.navigate("localhost:6080/new-password");
    }

    public void navigateToMailbox(Page page) {
        page.navigate("localhost:1080");
    }// Integration function end: Email
}
