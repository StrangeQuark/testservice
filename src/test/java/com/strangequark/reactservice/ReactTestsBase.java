// Integration file: React

package com.strangequark.reactservice;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import org.junit.jupiter.api.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReactTestsBase {
    public Playwright playwright;
    public Page page;
    public Browser browser;
    public ReactFunctions reactFunctions;
    public final Locator.WaitForOptions WAIT_FOR_VISIBLE = new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    public final Locator.WaitForOptions WAIT_FOR_DETACHED = new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED);
    public final Locator.WaitForOptions WAIT_FOR_ATTACHED = new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED);
    private static ExtentReports extent;
    private ExtentTest test;

    public static APIRequestContext apiRequestContext; // Integration function start: Auth
    public AuthFunctions authFunctions;
    public String username;
    public String email;
    public String password;// Integration function end: Auth
    public String collectionName;// Integration function start: File
    public String textFileName;
    public Path textFilePath;
    public String audioFileName;
    public Path audioFilePath;
    public String imageFileName;
    public Path imageFilePath;
    public String videoFileName;
    public Path videoFilePath;// Integration function end: File
    public String serviceName;// Integration function start: Vault
    public String environmentName;
    public String testVariableKey;
    public String testVariableValue;
    public String envFileName;
    public Path envFilePath;
    // Integration function end: Vault

    @BeforeAll
    public void beforeAll() throws URISyntaxException {
        playwright = Playwright.create();
        reactFunctions = new ReactFunctions();
        apiRequestContext = playwright.request().newContext(); // Integration line: Auth
        authFunctions = new AuthFunctions(apiRequestContext);// Integration line: Auth
        textFileName = "testUploadFile.txt";// Integration function start: File
        textFilePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + textFileName).toURI());
        audioFileName = "testAudioFile.mp3";
        audioFilePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + audioFileName).toURI());
        imageFileName = "testImageFile.png";
        imageFilePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + imageFileName).toURI());
        videoFileName = "testVideoFile.webm";
        videoFilePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + videoFileName).toURI());
        // Integration function end: File
        // Integration function start: Vault
        envFileName = "testEnvFile.env";
        envFilePath = Paths.get(getClass().getClassLoader().getResource("vaultserviceTestFiles/" + envFileName).toURI());
        // Integration function end: Vault

        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("test-results/react-report.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        // Integration function start: Auth
        if(testInfo.getTestMethod().get().getName().startsWith("user")) {
            username = "test_" + UUID.randomUUID();
            email = username + "@testEmail.com";
            password = "testPassword123!";
        }// Integration function end: Auth
        collectionName = "collection_" + UUID.randomUUID();// Integration line: File
        serviceName = "service_" + UUID.randomUUID();// Integration function start: Vault
        environmentName = "environment_" + UUID.randomUUID();
        testVariableKey = "testKey_" + UUID.randomUUID();
        testVariableValue = "testValue_" + UUID.randomUUID();
        // Integration function end: Vault

        test = extent.createTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        browser.close();
        page.close();
        // Integration function start: Auth
        if(testInfo.getTestMethod().get().getName().startsWith("user")) {
            authFunctions.deleteUser(username, email, password);
            assertFalse(authFunctions.getUserId(username, password).ok(), "User cleanup failed in React service register test");
        }// Integration function end: Auth
    }

    @AfterAll
    static void afterAll() {
        extent.flush();
    }
}
