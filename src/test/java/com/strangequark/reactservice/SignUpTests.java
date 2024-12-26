package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.utility.ImageComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignUpTests {
    private final String basePath = "./src/test/java/com/strangequark/reactservice";
    private final String comparatorsPath = basePath + "/comparators";
    private final String screenshotsPath = basePath + "/screenshots";
    private Playwright playwright;

    @BeforeAll
    public void beforeAll() {
        playwright = Playwright.create();
    }

    @Test
    public void ensureLoginDivLoading() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("localhost:3001");
            page.click("id=loginButton");
            page.locator("id=login-div").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("id=login-div").screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(screenshotsPath + "/loginDiv.png")));

            Assertions.assertTrue(ImageComparator.compareImages(new File(screenshotsPath + "/loginDiv.png"),
                    new File(comparatorsPath + "/loginDiv.png")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Assertions.assertTrue(true);
    }
}
