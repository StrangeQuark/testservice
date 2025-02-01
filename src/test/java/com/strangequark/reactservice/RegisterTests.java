package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.utility.ImageComparator;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterTests {
    private final String basePath = "./src/test/java/com/strangequark/reactservice";
    private final String comparatorsPath = basePath + "/comparators";
    private final String screenshotsPath = basePath + "/screenshots";
    private Playwright playwright;
    private Page page;
    private Browser browser;

    @BeforeAll
    public void beforeAll() {
        playwright = Playwright.create();
    }

    @BeforeEach
    public void beforeEach() {
        browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @Test
    public void ensureLoginDivLoading() {
        try {
            page.navigate("localhost:3001");
            page.click("id=loginButton");
            page.locator("id=login-div").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("id=login-div").screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(screenshotsPath + "/loginDiv.png")));

            Assertions.assertTrue(ImageComparator.compareImages(new File(screenshotsPath + "/loginDiv.png"),
                    new File(comparatorsPath + "/loginDiv.png")));
        } catch (Exception ex) {
            Assertions.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Test
    public void ensureRegisterDivLoading() {
        try {
            page.navigate("localhost:3001");
            page.click("id=loginButton");
            page.click("id=sign-up-link");
            page.locator("id=register-div").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("id=register-div").screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(screenshotsPath + "/registerDiv.png")));

            Assertions.assertTrue(ImageComparator.compareImages(new File(screenshotsPath + "/registerDiv.png"),
                    new File(comparatorsPath + "/registerDiv.png")));
        } catch (Exception ex) {
            Assertions.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Test
    public void registerTest() {
        try {
            page.navigate("localhost:3001");
            page.click("id=loginButton");
            page.click("id=sign-up-link");
            page.locator("id=username").fill("testUsername");
            page.locator("id=email").fill("test@email.com");
            page.locator("id=password").fill("testPassword");
            page.locator("id=confirm-password").fill("testPassword");
            page.click("id=submit-button");

            page.locator("id=request-success-text-field").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("id=request-success-text-field").screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(screenshotsPath + "/registerSuccess.png")));

            Assertions.assertTrue(ImageComparator.compareImages(new File(screenshotsPath + "/registerSuccess.png"),
                    new File(comparatorsPath + "/registerSuccess.png")));
        } catch (Exception ex) {
            Assertions.fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @AfterEach
    public void afterEach() {
        browser.close();
        page.close();
    }
}
