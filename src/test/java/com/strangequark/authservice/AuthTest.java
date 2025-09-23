package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.AuthUtility;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private final AuthUtility authUtility = new AuthUtility();

    private String testUsername;
    private String testEmail;
    private String testPassword;

    private static final String BASE_URL = "http://localhost:6001/api/auth";

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        // Skip teardown for healthcheckTest
        if (testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("deleteUserTest")  ||
                testInfo.getTestMethod().get().getName().equals("serviceAccountAuthenticationTest")) {
            return;
        }

        enableUser(testEmail);// Integration line: Email
        APIResponse response = authenticate(testUsername, testPassword);
        response = serveAccessToken(extractJwt(response));
        response = deleteUser(testUsername, testPassword, extractJwt(response));
        if (!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        }
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = healthcheck();
        assertTrue(response.ok(), "Healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void enableUserTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
    }// Integration function start: end

    @Test
    public void authenticateTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serveAccessTokenTest() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getUserIdTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void searchUsersByUsernameTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users by username failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void searchUsersByEmailTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = searchUsers(testEmail, accessToken);
        assertTrue(response.ok(), "Search users by email failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void getUserDetailsByIdsTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());

        List<String> ids = new ArrayList<>();
        ids.add(response.text().replace("\"", ""));

        response = getUserDetailsByIds(ids, accessToken);
        assertTrue(response.ok(), "Get user details by ids failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void disableUserByUsernameTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = disableUser(testUsername, accessToken);
        assertTrue(response.ok(), "Disable user failed: " + response.status() + " - " + response.text());

        response = authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Disable user failed - User still enabled: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("User is disabled", jsonObject.get("errorMessage").getAsString());
    }

    @Test
    public void updatePasswordTest() {
        String accessToken = registerEnableAuthenticateAccess();
        String newPassword = "newTestPassword";

        APIResponse response = updatePassword(testPassword, newPassword, accessToken);
        assertTrue(response.ok(), "Update password failed: " + response.status() + " - " + response.text());

        // Set the testPassword to the newPassword for afterEach method
        testPassword = newPassword;
        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Update password login attempt failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void addAuthorizationsToUserTest() {
        APIResponse response = bootstrapSuperUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Bootstrap super user failed: " + response.status() + " - " + response.text());

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        String accessToken = extractJwt(response);
        List<String> auths = new ArrayList<>();
        auths.add("Auth 1");
        auths.add("Auth 2");

        response = addAuthorizationsToUser(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Add authorizations test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("Authorizations successfully added", jsonObject.get("message").getAsString());
    }

    @Test
    public void removeAuthorizationsTest() {
        String accessToken = registerEnableAuthenticateAccess();
        List<String> auths = new ArrayList<>();
        auths.add("Auth 1");

        APIResponse response = removeAuthorizations(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Remove authorizations test failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void deleteUserTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = deleteUser(testUsername, testPassword, accessToken);
        assertTrue(response.ok(), "Delete user test failed: " + response.status() + " - " + response.text());

        response = authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Delete user test failed - Able to authenticate: " + response.status() + " - " + response.text());
    }

    @Test
    public void updateEmailTest() {
        String accessToken = registerEnableAuthenticateAccess();
        testEmail = "new@email.com";

        APIResponse response = updateEmail(testEmail, testPassword, accessToken);
        assertTrue(response.ok(), "Update email failed: " + response.status() + " - " + response.text());

        response = searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void updateUsernameTest() {
        String accessToken = registerEnableAuthenticateAccess();
        testUsername = "newUsername";

        APIResponse response = updateUsername(testUsername, testPassword, accessToken);
        assertTrue(response.ok(), "Update username failed: " + response.status() + " - " + response.text());

        // We have to re-fetch the accessToken since the user's username has changed
        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        accessToken = extractJwt(response);

        response = searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
    }

    @Test
    public void updateRoleTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = updateRole("SUPER", testUsername,  accessToken);
        assertFalse(response.ok(), "Update role failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void sendPasswordResetEmailTest() {
        String accessToken = registerEnableAuthenticateAccess();

        APIResponse response = sendPasswordResetEmail(testEmail, accessToken);
        assertTrue(response.ok(), "Send password reset email failed: " + response.status() + " - " + response.text());
    } // Integration function end: Email

    @Test
    public void bootstrapSuperUserTest() {
        APIResponse response = bootstrapSuperUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Bootstrap super user failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serviceAccountAuthenticationTest() {
        APIResponse response = serviceAccountAuthenticate("test", authUtility.getServiceSecretTest());
        assertTrue(response.ok(), "Service account authentication failed: " + response.status() + " - " + response.text());
    }

    private APIResponse healthcheck() {
        return apiRequestContext.get(BASE_URL + "/health");
    }

    private APIResponse register(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/register", RequestOptions.create().setData(requestBody));
    }
    // Integration function start: Email
    private APIResponse enableUser(String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(BASE_URL + "/user/enable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()));
    }// Integration function end: Email

    private APIResponse disableUser(String username, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);

        return apiRequestContext.post(BASE_URL + "/user/disable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse authenticate(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/authenticate", RequestOptions.create().setData(requestBody));
    }

    private APIResponse serveAccessToken(String refreshToken) {
        return apiRequestContext.get(BASE_URL + "/access", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + refreshToken));
    }

    private APIResponse getUserId(String username, String accessToken) {
        return apiRequestContext.get(BASE_URL + "/user/get-user-id?username=" + username, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse searchUsers(String query, String accessToken) {
        return apiRequestContext.get(BASE_URL + "/user/search-users?query=" + query, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse getUserDetailsByIds(List<String> ids, String accessToken) {
        return apiRequestContext.post(BASE_URL + "/user/get-user-details-by-ids", RequestOptions.create()
                .setData(ids)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse deleteUser(String username, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/user/delete-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse updatePassword(String password, String newPassword, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("password", password);
        requestBody.put("newPassword", newPassword);

        return apiRequestContext.post(BASE_URL + "/user/update-password", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse updateEmail(String newEmail, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newEmail", newEmail);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/user/update-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse updateUsername(String newUsername, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newUsername", newUsername);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/user/update-username", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse updateRole(String role, String username, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newRole", role);
        requestBody.put("username", username);

        return apiRequestContext.post(BASE_URL + "/user/update-role", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse sendPasswordResetEmail(String email, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(BASE_URL + "/user/send-password-reset-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse bootstrapSuperUser(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(BASE_URL + "/internal/bootstrap", RequestOptions.create().setData(requestBody)
                .setHeader("X-BOOTSTRAP-SECRET", authUtility.getAuthBootstrapSecretKey()));
    }

    private APIResponse serviceAccountAuthenticate(String clientId, String clientPassword) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("clientId", clientId);
        requestBody.put("clientPassword", clientPassword);

        return apiRequestContext.post(BASE_URL + "/service-account/authenticate", RequestOptions.create().setData(requestBody));
    }

    private APIResponse addAuthorizationsToUser(String username, List<String> auths,  String accessToken) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("authorizations", auths);

        return apiRequestContext.post(BASE_URL + "/user/add-authorizations-to-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse removeAuthorizations(String username, List<String> auths, String accessToken) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("authorizations", auths);

        return apiRequestContext.post(BASE_URL + "/user/remove-authorizations", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    private String registerEnableAuthenticateAccess() {
        APIResponse response = register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        return extractJwt(response);
    }

    private String extractJwt(APIResponse response) {
        if (response.ok()) {
            JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
            return jsonObject.get("jwtToken").getAsString();
        } else {
            throw new RuntimeException("Unable to extract JWT from failed response: " + response.status());
        }
    }
}
