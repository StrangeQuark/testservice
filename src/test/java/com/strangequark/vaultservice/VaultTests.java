// Integration file: Vault

package com.strangequark.vaultservice;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VaultTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static VaultFunctions vaultFunctions;
    private static AuthFunctions authFunctions; // Integration line: Auth

    private String testServiceName;
    private String testEnvironmentName;
    private String testVariableName;
    private String testVariableValue;

    private final String ENV_TEST_FILE = "testEnvFile.env";

    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
        vaultFunctions = new VaultFunctions(apiRequestContext
                , authFunctions // Integration line: Auth
        );

        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("test-results/vault-report.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("createServiceTest") ||
                testInfo.getTestMethod().get().getName().equals("createEnvironmentTest")) {
            return;
        }
        testServiceName = "testService_" + UUID.randomUUID();
        testEnvironmentName = "testEnvironment_" + UUID.randomUUID();
        testVariableName = "testVariable_" + UUID.randomUUID();
        testVariableValue = "testValue_" + UUID.randomUUID();

        APIResponse response = vaultFunctions.createService(testServiceName);
        assertTrue(response.ok(), "Create service setup failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.createEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Create environment setup failed: " + response.status() + " - " + response.text());

        if(!testInfo.getTestMethod().get().getName().equals("addVariableTest") &&
                !testInfo.getTestMethod().get().getName().equals("downloadEnvFileTest")) {
            response = vaultFunctions.addVariable(testServiceName, testEnvironmentName, testVariableName, testVariableValue);
            assertTrue(response.ok(), "Add variable setup failed: " + response.status() + " - " + response.text());
        }

        test = extent.createTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("deleteServiceTest")) {
            return;
        }

        APIResponse response = vaultFunctions.deleteService(testServiceName);
        if(!response.ok()) {
            System.err.println("Cleanup failed for " + testServiceName + ": " + response.status() + " - " + response.text());
        }
    }

    @AfterAll
    static void afterAll() {
        extent.flush();
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = vaultFunctions.healthcheck();
        assertTrue(response.ok(), "Vault service healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void createServiceTest() {
        testServiceName = "testService_" + UUID.randomUUID();

        APIResponse response = vaultFunctions.createService(testServiceName);
        assertTrue(response.ok(), "Create service test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void createEnvironmentTest() {
        testServiceName = "testService_" + UUID.randomUUID();
        testEnvironmentName = "testEnvironment_" + UUID.randomUUID();

        APIResponse response = vaultFunctions.createService(testServiceName);
        assertTrue(response.ok(), "Create service in create environment test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.createEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Create environment test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getServiceTest() {
        APIResponse response = vaultFunctions.getService(testServiceName);
        assertTrue(response.ok(), "Get service test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getEnvironmentsByServiceTest() {
        APIResponse response = vaultFunctions.getEnvironmentsByService(testServiceName);
        assertTrue(response.ok(), "Get environments by service test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get environments by service return size failed");

        assertTrue(jsonArray.toString().contains(testEnvironmentName), "Get environments by service return failed");
    }

    @Test
    public void getEnvironmentTest() {
        APIResponse response = vaultFunctions.getEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Get environment test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testEnvironmentName, jsonObject.get("name").getAsString());
    }

    @Test
    public void getVariablesByServiceTest() {
        APIResponse response = vaultFunctions.getVariablesByService(testServiceName);
        assertTrue(response.ok(), "Get variables by service test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get variables by service return size failed");

        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        assertEquals(testVariableName, jsonObject.get("key").getAsString(), "Get variables by service return variable key test failed");
        assertEquals(testVariableValue, jsonObject.get("value").getAsString(), "Get variables by service return variable value test failed");
    }

    @Test
    public void getVariablesByEnvironmentTest() {
        APIResponse response = vaultFunctions.getVariablesByEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Get variables by environment test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get variables by environment return size failed");

        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        assertEquals(testVariableName, jsonObject.get("key").getAsString(), "Get variables by environment return variable key test failed");
        assertEquals(testVariableValue, jsonObject.get("value").getAsString(), "Get variables by environment return variable value test failed");
    }

    @Test
    public void getVariableByNameTest() {
        APIResponse response = vaultFunctions.getVariableByName(testServiceName, testEnvironmentName, testVariableName);
        assertTrue(response.ok(), "Get variable by name test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testVariableName, jsonObject.get("key").getAsString(), "Get variable by name return variable key test failed");
        assertEquals(testVariableValue, jsonObject.get("value").getAsString(), "Get variable by name return variable value test failed");
    }

    @Test
    public void addVariableTest() {
        APIResponse response = vaultFunctions.addVariable(testServiceName, testEnvironmentName, testVariableName, testVariableValue);
        assertTrue(response.ok(), "Add variable test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getVariableByName(testServiceName, testEnvironmentName, testVariableName);
        assertTrue(response.ok(), "Get variable by name in add variable test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testVariableName, jsonObject.get("key").getAsString(), "Get variable by name in add variable return variable key test failed");
        assertEquals(testVariableValue, jsonObject.get("value").getAsString(), "Get variable by name in add variable return variable value test failed");
    }

    @Test
    public void updateVariableTest() {
        testVariableValue = "updatedTestVariableValue";

        APIResponse response = vaultFunctions.updateVariable(testServiceName, testEnvironmentName, testVariableName, testVariableValue);
        assertTrue(response.ok(), "Update variable test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getVariableByName(testServiceName, testEnvironmentName, testVariableName);
        assertTrue(response.ok(), "Get variable by name in update variable test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testVariableName, jsonObject.get("key").getAsString(), "Get variable by name in update variable return variable key test failed");
        assertEquals(testVariableValue, jsonObject.get("value").getAsString(), "Get variable by name in update variable return variable value test failed");
    }

    @Test
    public void addEnvFileTest() {
        APIResponse response = vaultFunctions.addEnvFile(testServiceName, testEnvironmentName, ENV_TEST_FILE);
        assertTrue(response.ok(), "Add env file test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getVariablesByEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Get variables by environment in add env file test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Get variables by environment in add env file return size failed");

        String returnString = jsonArray.toString();
        assertTrue(returnString.contains("testEnvKey"), "Add env file return testEnvVariable key test failed");
        assertTrue(returnString.contains("testEnvValue"), "Add env file return testEnvVariable value test failed");
    }

    @Test
    public void downloadEnvFileTest() {
        APIResponse response = vaultFunctions.addEnvFile(testServiceName, testEnvironmentName, ENV_TEST_FILE);
        assertTrue(response.ok(), "File upload step in download env file test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.downloadEnvFile(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Env file download test failed: " + response.status() + " - " + response.text());

        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("vaultserviceTestFiles/" + ENV_TEST_FILE).toURI());
            assertTrue(Files.readString(filePath).contains(response.text()), "Download env file contents test failed: " + response.status() + " - " + response.text());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load test env file from resources", ex);
        }
    }

    @Test
    public void deleteVariableTest() {
        APIResponse response = vaultFunctions.deleteVariable(testServiceName, testEnvironmentName, testVariableName);
        assertTrue(response.ok(), "Delete variable test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getVariablesByEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Get variables by environment in delete variable test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(0, jsonArray.size(), "Get variables by environment in delete variable return size failed");
    }

    @Test
    public void deleteEnvironmentTest() {
        APIResponse response = vaultFunctions.deleteEnvironment(testServiceName, testEnvironmentName);
        assertTrue(response.ok(), "Delete environment test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getEnvironmentsByService(testServiceName);
        assertTrue(response.ok(), "Get environments by service in delete environment test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(0, jsonArray.size(), "Get environments by service in delete environment return size failed");
    }

    @Test
    public void deleteServiceTest() {
        APIResponse response = vaultFunctions.deleteService(testServiceName);
        assertTrue(response.ok(), "Delete service test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getService(testServiceName);
        assertFalse(response.ok(), "Get service in delete service test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getAllServicesTest() {
        APIResponse response = vaultFunctions.getAllServices();
        assertTrue(response.ok(), "Get all services test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get all services return size failed");

        assertTrue(jsonArray.toString().contains(testServiceName), "Get all services return failed");
    }
    // Integration function start: Auth
    @Test
    public void getUsersByServiceTest() {
        APIResponse response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Get users by service test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Get users by service return size test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getAllRolesTest() {
        APIResponse response = vaultFunctions.getAllRoles();
        assertTrue(response.ok(), "Get all roles test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(3, jsonArray.size(), "Get all roles return size test failed: " + response.status() + " - " + response.text());

        String returnString = jsonArray.toString();
        assertTrue(returnString.contains("OWNER"), "Vault service should contain OWNER role");
        assertTrue(returnString.contains("MANAGER"), "Vault service should contain MANAGER role");
        assertTrue(returnString.contains("MAINTAINER"), "Vault service should contain MAINTAINER role");
    }

    @Test
    public void getCurrentUserRoleTest() {
        APIResponse response = vaultFunctions.getCurrentUserRole(testServiceName);
        assertTrue(response.ok(), "Get current user role test failed: " + response.status() + " - " + response.text());
        assertEquals("OWNER", response.text().replace("\"", ""));
    }

    @Test
    public void updateUserRoleTest() {
        vaultFunctions.testUsername = "test_" + UUID.randomUUID();
        vaultFunctions.testEmail = vaultFunctions.testUsername + "@email.com";
        vaultFunctions.testPassword = UUID.randomUUID().toString();

        authFunctions.registerEnableAuthenticateAccess(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);

        APIResponse response = vaultFunctions.addUserToService(testServiceName);
        assertTrue(response.ok(), "Add user to service in update user role test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.updateUserRole(testServiceName, "MANAGER");
        assertTrue(response.ok(), "Update user role test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Get users by service in update user role test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertTrue(jsonArray.toString().contains("MANAGER"), "Users list should contain newly update MANAGER role in update user role test");

        authFunctions.deleteUser(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);
        assertTrue(response.ok(), "Delete user in update user role test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void addUserToCollectionTest() {
        vaultFunctions.testUsername = "test_" + UUID.randomUUID();
        vaultFunctions.testEmail = vaultFunctions.testUsername + "@email.com";
        vaultFunctions.testPassword = UUID.randomUUID().toString();

        authFunctions.registerEnableAuthenticateAccess(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);

        APIResponse response = vaultFunctions.addUserToService(testServiceName);
        assertTrue(response.ok(), "Add user to service test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Get users by service in add user to service test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Add user to service return size test failed: " + response.status() + " - " + response.text());

        authFunctions.deleteUser(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);
        assertTrue(response.ok(), "Delete user in add user to service test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void deleteUserFromCollectionTest() {
        vaultFunctions.testUsername = "test_" + UUID.randomUUID();
        vaultFunctions.testEmail = vaultFunctions.testUsername + "@email.com";
        vaultFunctions.testPassword = UUID.randomUUID().toString();

        authFunctions.registerEnableAuthenticateAccess(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);

        APIResponse response = vaultFunctions.addUserToService(testServiceName);
        assertTrue(response.ok(), "Add user to service in delete user from service test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Get users by service in delete user from service test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Add user to service in delete user from service return size test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.deleteUserFromService(testServiceName);
        assertTrue(response.ok(), "Delete user from service test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Second get users by service in delete user from service test failed: " + response.status() + " - " + response.text());

        jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Delete user from service return size test failed: " + response.status() + " - " + response.text());

        authFunctions.deleteUser(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);
        assertTrue(response.ok(), "Delete user in delete user from service test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void deleteUserFromAllCollectionsTest() {
        vaultFunctions.testUsername = "test_" + UUID.randomUUID();
        vaultFunctions.testEmail = vaultFunctions.testUsername + "@email.com";
        vaultFunctions.testPassword = UUID.randomUUID().toString();

        authFunctions.registerEnableAuthenticateAccess(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);

        APIResponse response = vaultFunctions.addUserToService(testServiceName);
        assertTrue(response.ok(), "Add user to service in delete user from all services test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Get users by service in delete user from all services test failed: " + response.status() + " - " + response.text());

        JsonArray jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(2, jsonArray.size(), "Add user to service in delete user from all services return size test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.deleteUserFromAllServices();
        assertTrue(response.ok(), "Delete user from all services test failed: " + response.status() + " - " + response.text());

        response = vaultFunctions.getUsersByService(testServiceName);
        assertTrue(response.ok(), "Second get users by service in delete user from all services test failed: " + response.status() + " - " + response.text());

        jsonArray = JsonParser.parseString(response.text()).getAsJsonArray();
        assertEquals(1, jsonArray.size(), "Delete user from all services return size test failed: " + response.status() + " - " + response.text());

        authFunctions.deleteUser(vaultFunctions.testUsername, vaultFunctions.testEmail, vaultFunctions.testPassword);
        assertTrue(response.ok(), "Delete user in delete user from all services test failed: " + response.status() + " - " + response.text());
    } // Integration function end: Auth
}
