// Integration file: Vault

package com.strangequark.vaultservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import com.strangequark.utility.AuthUtility; // Integration line: Auth
import com.strangequark.utility.EnvUtility;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VaultFunctions {
    APIRequestContext apiRequestContext;
    AuthFunctions authFunctions; // Integration function start: Auth
    AuthUtility authUtility;

    public String testUsername;
    public String testEmail;
    public String testPassword;// Integration function end: Auth

    public static final String VAULT_BASE_URL = EnvUtility.getEnvVar("VAULT_BASE_URL");

    public VaultFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
    }
    // Integration function start: Auth
    public VaultFunctions(APIRequestContext apiRequestContext, AuthFunctions authFunctions) {
        this(apiRequestContext);
        this.authFunctions = authFunctions;
        this.authUtility = new AuthUtility(apiRequestContext); // Integration line: Auth
    } // Integration function end: Auth

    public APIResponse healthcheck() {
        return apiRequestContext.get(VAULT_BASE_URL + "/health");
    }

    public APIResponse createService(String testServiceName) {
        return apiRequestContext.post(VAULT_BASE_URL + "/create-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse createEnvironment(String testServiceName, String testEnvironmentName) {
        return apiRequestContext.post(VAULT_BASE_URL + "/create-environment/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getService(String testServiceName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getEnvironmentsByService(String testServiceName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-environments-by-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getEnvironment(String testServiceName, String testEnvironmentName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-environment/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getVariablesByService(String testServiceName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-variables-by-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getVariablesByEnvironment(String testServiceName, String testEnvironmentName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-variables-by-environment/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getVariableByName(String testServiceName, String testEnvironmentName, String testVariableName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-variable-by-name/" + testServiceName + "/" + testEnvironmentName + "/" + testVariableName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse addVariable(String testServiceName, String testEnvironmentName, String key, String value) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", key);
        requestBody.put("value", value);

        return apiRequestContext.post(VAULT_BASE_URL + "/add-variable/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse updateVariable(String testServiceName, String testEnvironmentName, String key, String value) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", key);
        requestBody.put("value", value);

        return apiRequestContext.post(VAULT_BASE_URL + "/update-variable/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse updateVariables(String testServiceName, String testEnvironmentName, String key, String value) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", key);
        requestBody.put("value", value);

        List<Map<String, String>> requestList = new ArrayList<>();
        requestList.add(requestBody);

        return apiRequestContext.post(VAULT_BASE_URL + "/update-variables/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create().setData(requestList)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse addEnvFile(String testServiceName, String testEnvironmentName, String uploadFileName) {
        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("vaultserviceTestFiles/" + uploadFileName).toURI());
            FormData formData = FormData.create().set("file", filePath);

            return apiRequestContext.post(VAULT_BASE_URL + "/add-env-file/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create().setMultipart(formData)
                    .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
            );
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to load test file from resources", ex);
        }
    }

    public APIResponse downloadEnvFile(String testServiceName, String testEnvironmentName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/download-env-file/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteVariable(String testServiceName, String testEnvironmentName, String testVariableName) {
        return apiRequestContext.delete(VAULT_BASE_URL + "/delete-variable/" + testServiceName + "/" + testEnvironmentName + "/" + testVariableName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteEnvironment(String testServiceName, String testEnvironmentName) {
        return apiRequestContext.delete(VAULT_BASE_URL + "/delete-environment/" + testServiceName + "/" + testEnvironmentName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteService(String testServiceName) {
        return apiRequestContext.delete(VAULT_BASE_URL + "/delete-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getAllServices() {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-all-services", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }
    // Integration function start: Auth
    public APIResponse getUsersByService(String testServiceName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-users-by-service/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getAllRoles() {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-all-roles", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getCurrentUserRole(String testServiceName) {
        return apiRequestContext.get(VAULT_BASE_URL + "/get-current-user-role/" + testServiceName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse updateUserRole(String testServiceName, String newRole) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("serviceName", testServiceName);
        requestBody.put("username", testUsername);
        requestBody.put("role", newRole);

        return apiRequestContext.post(VAULT_BASE_URL + "/update-user-role", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse addUserToService(String testServiceName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("serviceName", testServiceName);
        requestBody.put("username", testUsername);
        requestBody.put("role", "MAINTAINER");

        return apiRequestContext.post(VAULT_BASE_URL + "/add-user-to-service", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteUserFromService(String testServiceName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("serviceName", testServiceName);
        requestBody.put("username", testUsername);

        return apiRequestContext.post(VAULT_BASE_URL + "/delete-user-from-service", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteUserFromAllServices() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", testUsername);

        return apiRequestContext.post(VAULT_BASE_URL + "/delete-user-from-all-services", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }// Integration function end: Auth
}
