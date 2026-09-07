// Integration file: Auth

package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExtentTestWatcher.class)
public class AuthTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static AuthFunctions authFunctions;

    private String testUsername;
    private String testEmail;
    private String testPassword;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        authFunctions = new AuthFunctions(apiRequestContext);
    }

    @AfterAll
    public static void afterAll() {
        apiRequestContext.dispose();
        playwright.close();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest")) {
            return;
        }

        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        if(testInfo.getTestMethod().get().getName().equals("healthcheckTest") ||
                testInfo.getTestMethod().get().getName().equals("deleteUserTest")  ||
                testInfo.getTestMethod().get().getName().equals("serviceAccountAuthenticationTest") ||
                testInfo.getTestMethod().get().getName().equals("authorizationManagementTest") ||
                testInfo.getTestMethod().get().getName().equals("invitationRequiresAuthorizationTest")) {
            return;
        }

        APIResponse response = authFunctions.deleteUser(testUsername, testEmail, testPassword);
        if(!response.ok()) {
            System.err.println("Cleanup failed for " + testUsername + ": " + response.status() + " - " + response.text());
        }
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = authFunctions.healthcheck();
        assertTrue(response.ok(), "Auth service healthcheck failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void registerTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void invitationOnlyRegistrationTest() {
        APIResponse response = authFunctions.getInviteOnly();
        assertTrue(response.ok(), "Invite-only status failed: " + response.status() + " - " + response.text());
        assertTrue(JsonParser.parseString(response.text()).getAsJsonObject().get("inviteOnly").getAsBoolean(), "Invite-only mode should be enabled");

        response = authFunctions.registerWithoutInvitation(testUsername, testEmail, testPassword);
        assertFalse(response.ok(), "Registration without an invitation should fail");

        String superAccessToken = authFunctions.authenticateInitialSuperUser();
        String inviteToken = authFunctions.createInvitationToken(testEmail, superAccessToken);

        response = authFunctions.register(testUsername, testEmail, testPassword, inviteToken);
        assertTrue(response.ok(), "Registration with an invitation failed: " + response.status() + " - " + response.text());

        response = authFunctions.register("second_" + testUsername, testEmail, testPassword, inviteToken);
        assertFalse(response.ok(), "Used invitation should fail");
    }

    @Test
    public void invitationRequiresAuthorizationTest() {
        APIResponse response = authFunctions.serviceAccountAuthenticate("test");
        String accessToken = authFunctions.extractJwt(response);

        response = authFunctions.createInvitation(testEmail, accessToken);
        assertEquals(403, response.status(), "Service account without invitation authorization should be rejected");
    }
    // Integration function start: Email
    @Test
    public void enableUserTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
    }// Integration function end: Email

    @Test
    public void enableUserRejectsNonEmailServiceAccountTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = authFunctions.enableUser(testEmail, "test");
        assertFalse(response.ok(), "Non-email service account should not enable users");
        assertEquals(403, response.status());
    }

    @Test
    public void enableUserRejectsUserTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.enableUserWithAccessToken(testEmail, accessToken);
        assertFalse(response.ok(), "Users should not enable users");
        assertEquals(403, response.status());
    }

    @Test
    public void enableUserAllowsSuperUserTest() {
        String superAccessToken = authFunctions.authenticateInitialSuperUser();

        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = authFunctions.enableUserWithAccessToken(testEmail, superAccessToken);
        assertTrue(response.ok(), "SUPER user should enable users: " + response.status() + " - " + response.text());
    }

    @Test
    public void enableUserAllowsAdminUserTest() {
        String superAccessToken = authFunctions.authenticateInitialSuperUser();

        String adminUsername = "test_" + UUID.randomUUID();
        String adminEmail = adminUsername + "@email.com";
        String adminPassword = UUID.randomUUID().toString();
        APIResponse response = authFunctions.register(adminUsername, adminEmail, adminPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        response = authFunctions.enableUser(adminEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());

        response = authFunctions.updateRole("ADMIN", adminUsername, superAccessToken);
        assertTrue(response.ok(), "Admin role assignment failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(adminUsername, adminPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        String adminAccessToken = authFunctions.extractJwt(response);

        response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());

        response = authFunctions.enableUserWithAccessToken(testEmail, adminAccessToken);
        assertTrue(response.ok(), "ADMIN user should enable users: " + response.status() + " - " + response.text());

        response = authFunctions.deleteUser(adminUsername, adminEmail, adminPassword);
        assertTrue(response.ok(), "Admin user cleanup failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void authenticateTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void serveAccessTokenTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void getUserIdTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void searchUsersByUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users by username failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void searchUsersByEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.searchUsers(testEmail, accessToken);
        assertTrue(response.ok(), "Search users by email failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void getUserDetailsByIdsTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.getUserId(testUsername, accessToken);
        assertTrue(response.ok(), "Get user id failed: " + response.status() + " - " + response.text());

        List<String> ids = new ArrayList<>();
        ids.add(response.text().replace("\"", ""));

        response = authFunctions.getUserDetailsByIds(ids, accessToken);
        assertTrue(response.ok(), "Get user details by ids failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void disableUserByUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.disableUser(testUsername, accessToken);
        assertTrue(response.ok(), "Disable user failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Disable user failed - User still enabled: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("User is disabled", jsonObject.get("errorMessage").getAsString());
    }

    @Test
    public void updatePasswordTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        String newPassword = "newTestPassword";

        APIResponse response = authFunctions.updatePassword(testPassword, newPassword, accessToken);
        assertTrue(response.ok(), "Update password failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        // Set the testPassword to the newPassword for afterEach method
        testPassword = newPassword;
        response = authFunctions.authenticate(testUsername, testPassword);
        assertTrue(response.ok(), "Update password login attempt failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void addAuthorizationsToUserTest() {
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());

        String accessToken = authFunctions.authenticateInitialSuperUser();
        List<String> auths = new ArrayList<>();
        auths.add("AUTH_1_" + UUID.randomUUID());
        auths.add("AUTH_2_" + UUID.randomUUID());

        response = authFunctions.createAuthorization(auths.get(0), accessToken);
        assertTrue(response.ok(), "First authorization creation failed: " + response.status() + " - " + response.text());
        response = authFunctions.createAuthorization(auths.get(1), accessToken);
        assertTrue(response.ok(), "Second authorization creation failed: " + response.status() + " - " + response.text());

        response = authFunctions.addAuthorizationsToUser(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Add authorizations test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals("Authorizations successfully added", jsonObject.get("message").getAsString());

        response = authFunctions.removeAuthorizations(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Authorization cleanup failed: " + response.status() + " - " + response.text());
        response = authFunctions.deleteAuthorization(auths.get(0), accessToken);
        assertTrue(response.ok(), "First authorization deletion failed: " + response.status() + " - " + response.text());
        response = authFunctions.deleteAuthorization(auths.get(1), accessToken);
        assertTrue(response.ok(), "Second authorization deletion failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void removeAuthorizationsTest() {
        String accessToken = authFunctions.authenticateInitialSuperUser();
        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());
        List<String> auths = new ArrayList<>();
        auths.add("AUTH_" + UUID.randomUUID());

        response = authFunctions.createAuthorization(auths.getFirst(), accessToken);
        assertTrue(response.ok(), "Authorization creation failed: " + response.status() + " - " + response.text());
        response = authFunctions.addAuthorizationsToUser(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Authorization setup failed: " + response.status() + " - " + response.text());
        response = authFunctions.removeAuthorizations(testUsername, auths, accessToken);
        assertTrue(response.ok(), "Remove authorizations test failed: " + response.status() + " - " + response.text());

        response = authFunctions.deleteAuthorization(auths.getFirst(), accessToken);
        assertTrue(response.ok(), "Authorization cleanup failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void authorizationManagementTest() {
        String accessToken = authFunctions.authenticateInitialSuperUser();
        String authorization = "AUTH_" + UUID.randomUUID();

        APIResponse response = authFunctions.createAuthorization(authorization, accessToken);
        assertTrue(response.ok(), "Authorization creation failed: " + response.status() + " - " + response.text());

        response = authFunctions.addRoleAuthorization("ADMIN", authorization, accessToken);
        assertTrue(response.ok(), "Role authorization creation failed: " + response.status() + " - " + response.text());

        response = authFunctions.getRoleAuthorizations("ADMIN", accessToken);
        assertTrue(response.ok(), "Get role authorizations failed: " + response.status() + " - " + response.text());
        assertTrue(response.text().contains(authorization));

        response = authFunctions.removeRoleAuthorization("ADMIN", authorization, accessToken);
        assertTrue(response.ok(), "Role authorization deletion failed: " + response.status() + " - " + response.text());

        response = authFunctions.deleteAuthorization(authorization, accessToken);
        assertTrue(response.ok(), "Authorization deletion failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void authorizationManagementRequiresSuperUserTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.createAuthorization("AUTH_" + UUID.randomUUID(), accessToken);

        assertEquals(403, response.status());
    }

    @Test
    public void deleteUserTest() {
        authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.deleteUser(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Delete user test failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(testUsername, testPassword);
        assertFalse(response.ok(), "Delete user test failed - Able to authenticate: " + response.status() + " - " + response.text());
    }

    @Test
    public void updateEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        testEmail = "new@email.com";

        APIResponse response = authFunctions.updateEmail(testEmail, testPassword, accessToken);
        assertTrue(response.ok(), "Update email failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        accessToken = authFunctions.extractJwt(response);

        response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testEmail, jsonObject.get("email").getAsString());
    }

    @Test
    public void updateUsernameTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);
        testUsername = "newUsername";

        APIResponse response = authFunctions.updateUsername(testUsername, testPassword, accessToken);
        assertTrue(response.ok(), "Update username failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        accessToken = authFunctions.extractJwt(response);

        response = authFunctions.searchUsers(testUsername, accessToken);
        assertTrue(response.ok(), "Search users failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        assertEquals(testUsername, jsonObject.get("username").getAsString());
    }

    @Test
    public void logoutTest() {
        authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.logout();
        assertTrue(response.ok(), "Logout failed: " + response.status() + " - " + response.text());

        response = authFunctions.serveAccessToken();
        assertFalse(response.ok(), "Access token retrieval should fail after logout");
    }

    @Test
    public void updateRoleTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.updateRole("SUPER", testUsername,  accessToken);
        assertFalse(response.ok(), "Update role failed: " + response.status() + " - " + response.text());
    }

    @Test
    public void updateRoleRejectsAdminPromotionToSuperTest() {
        String superAccessToken = authFunctions.authenticateInitialSuperUser();

        String adminUsername = "test_" + UUID.randomUUID();
        String adminEmail = adminUsername + "@email.com";
        String adminPassword = UUID.randomUUID().toString();

        APIResponse response = authFunctions.register(adminUsername, adminEmail, adminPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        response = authFunctions.enableUser(adminEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());

        response = authFunctions.updateRole("ADMIN", adminUsername, superAccessToken);
        assertTrue(response.ok(), "Admin role assignment failed: " + response.status() + " - " + response.text());

        response = authFunctions.authenticate(adminUsername, adminPassword);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());
        response = authFunctions.serveAccessToken();
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());
        String adminAccessToken = authFunctions.extractJwt(response);

        response = authFunctions.updateRole("SUPER", adminUsername, adminAccessToken);
        assertFalse(response.ok(), "ADMIN user should not promote itself to SUPER");
        assertEquals(400, response.status());

        response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        response = authFunctions.enableUser(testEmail);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text());

        response = authFunctions.updateRole("SUPER", testUsername, adminAccessToken);
        assertFalse(response.ok(), "ADMIN user should not promote another user to SUPER");
        assertEquals(400, response.status());

        response = authFunctions.deleteUser(adminUsername, adminEmail, adminPassword);
        assertTrue(response.ok(), "Admin user cleanup failed: " + response.status() + " - " + response.text());
    }
    // Integration function start: Email
    @Test
    public void sendPasswordResetEmailTest() {
        String accessToken = authFunctions.registerEnableAuthenticateAccess(testUsername, testEmail, testPassword);

        APIResponse response = authFunctions.sendPasswordResetEmail(testEmail, accessToken);
        assertTrue(response.ok(), "Send password reset email failed: " + response.status() + " - " + response.text());

        APIResponse missingUserResponse = authFunctions.sendPasswordResetEmail("missing_" + UUID.randomUUID() + "@email.com", accessToken);
        assertEquals(response.status(), missingUserResponse.status());
        assertEquals(response.text(), missingUserResponse.text());
    } // Integration function end: Email

    @Test
    public void serviceAccountAuthenticationTest() {
        APIResponse response = authFunctions.serviceAccountAuthenticate("test");
        assertTrue(response.ok(), "Service account authentication failed: " + response.status() + " - " + response.text());
    }
}
