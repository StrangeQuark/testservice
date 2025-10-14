// Integration file: Auth

package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.AuthUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthFunctions {
    public static final String AUTH_BASE_URL = "http://localhost:6001/api/auth";

    private final APIRequestContext apiRequestContext;
    private final AuthUtility authUtility;

    public AuthFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
        this.authUtility = new AuthUtility(apiRequestContext);
    }

    public APIResponse healthcheck() {
        return apiRequestContext.get(AUTH_BASE_URL + "/health");
    }

    public APIResponse register(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(AUTH_BASE_URL + "/register", RequestOptions.create().setData(requestBody));
    }

    public APIResponse enableUser(String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/enable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()));
    }

    public APIResponse disableUser(String username, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/disable-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse authenticate(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return apiRequestContext.post(AUTH_BASE_URL + "/authenticate", RequestOptions.create().setData(requestBody));
    }

    public APIResponse serveAccessToken(String refreshToken) {
        return apiRequestContext.get(AUTH_BASE_URL + "/access", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + refreshToken));
    }

    public APIResponse getUserId(String username, String accessToken) {
        return apiRequestContext.get(AUTH_BASE_URL + "/user/get-user-id?username=" + username, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse searchUsers(String query, String accessToken) {
        return apiRequestContext.get(AUTH_BASE_URL + "/user/search-users?query=" + query, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse getUserDetailsByIds(List<String> ids, String accessToken) {
        return apiRequestContext.post(AUTH_BASE_URL + "/user/get-user-details-by-ids", RequestOptions.create()
                .setData(ids)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse deleteUser(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        enableUser(email);
        String refreshToken = extractJwt(authenticate(username, password));
        String accessToken = extractJwt(serveAccessToken(refreshToken));

        return apiRequestContext.post(AUTH_BASE_URL + "/user/delete-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse updatePassword(String password, String newPassword, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("password", password);
        requestBody.put("newPassword", newPassword);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/update-password", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse updateEmail(String newEmail, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newEmail", newEmail);
        requestBody.put("password", password);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/update-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse updateUsername(String newUsername, String password, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newUsername", newUsername);
        requestBody.put("password", password);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/update-username", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse updateRole(String role, String username, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newRole", role);
        requestBody.put("username", username);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/update-role", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse sendPasswordResetEmail(String email, String accessToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/send-password-reset-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse bootstrapSuperUser(String username, String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);

        return apiRequestContext.post(AUTH_BASE_URL + "/internal/bootstrap", RequestOptions.create().setData(requestBody)
                .setHeader("X-BOOTSTRAP-SECRET", authUtility.getAuthBootstrapSecretKey()));
    }

    public APIResponse serviceAccountAuthenticate(String clientId) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("clientId", clientId);
        requestBody.put("clientPassword", authUtility.getServiceSecretTest(clientId));

        return apiRequestContext.post(AUTH_BASE_URL + "/service-account/authenticate", RequestOptions.create().setData(requestBody));
    }

    public APIResponse addAuthorizationsToUser(String username, List<String> auths,  String accessToken) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("authorizations", auths);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/add-authorizations-to-user", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public APIResponse removeAuthorizations(String username, List<String> auths, String accessToken) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("authorizations", auths);

        return apiRequestContext.post(AUTH_BASE_URL + "/user/remove-authorizations", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken));
    }

    public String registerEnableAuthenticateAccess(String username, String email, String password) {
        APIResponse response = register(username, email, password);
        assertTrue(response.ok(), "Registration failed: " + response.status() + " - " + response.text());
        // Integration function start: Email
        response = enableUser(email);
        assertTrue(response.ok(), "Enablement failed: " + response.status() + " - " + response.text()); // Integration function end: Email

        response = authenticate(username, password);
        assertTrue(response.ok(), "Authentication failed: " + response.status() + " - " + response.text());

        response = serveAccessToken(extractJwt(response));
        assertTrue(response.ok(), "Access token retrieval failed: " + response.status() + " - " + response.text());

        return extractJwt(response);
    }

    public String extractJwt(APIResponse response) {
        if(response.ok()) {
            JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
            return jsonObject.get("jwtToken").getAsString();
        } else {
            throw new RuntimeException("Unable to extract JWT from failed response: " + response.status());
        }
    }
}
