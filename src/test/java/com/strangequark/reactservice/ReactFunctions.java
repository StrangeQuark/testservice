package com.strangequark.reactservice;

import com.microsoft.playwright.Page;

public class ReactFunctions {
    public ReactFunctions() {

    }
    // Integration function start: Auth
    public void navigateToLogin(Page page) {
        page.navigate("localhost:6080");
        page.click("id=loginButton");
    }

    public void navigateToRegister(Page page) {
        navigateToLogin(page);

        page.click("id=sign-up-link");
    }

    public void fillRegisterForm(Page page, String username, String email, String password) {
        page.locator("id=username").fill(username);
        page.locator("id=email").fill(email);
        page.locator("id=password").fill(password);
        page.locator("id=confirm-password").fill(password);

        page.click("id=submit-button");
    }// Integration function end: Auth
    // Integration function start: Email
    public void navigateToMailbox(Page page) {
        page.navigate("localhost:1080");
    }// Integration function end: Email
}
