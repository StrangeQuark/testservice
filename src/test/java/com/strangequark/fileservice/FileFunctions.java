// Integration file: File

package com.strangequark.fileservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import com.strangequark.utility.AuthUtility; // Integration line: Auth

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileFunctions {
    APIRequestContext apiRequestContext;
    AuthFunctions authFunctions; // Integration function start: Auth
    AuthUtility authUtility;

    public String testUsername;
    public String testEmail;
    public String testPassword;// Integration function end: Auth

    public static final String FILE_BASE_URL = "http://localhost:6010/api/file";

    public FileFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
    }
    // Integration function start: Auth
    public FileFunctions(APIRequestContext apiRequestContext, AuthFunctions authFunctions) {
        this(apiRequestContext);
        this.authFunctions = authFunctions;
        this.authUtility = new AuthUtility(apiRequestContext); // Integration line: Auth
    } // Integration function end: Auth

    public APIResponse healthcheck() {
        return apiRequestContext.get(FILE_BASE_URL + "/health");
    }

    public APIResponse createCollection(String testCollectionName) {
        return apiRequestContext.post(FILE_BASE_URL + "/new-collection/" + testCollectionName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getAllCollections() {
        return apiRequestContext.get(FILE_BASE_URL + "/get-all-collections", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteCollection(String testCollectionName) {
        return apiRequestContext.delete(FILE_BASE_URL + "/delete-collection/" + testCollectionName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse upload(String testCollectionName, String uploadFileName) {
        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/" + uploadFileName).toURI());
            FormData formData = FormData.create().set("file", filePath);

            return apiRequestContext.post(FILE_BASE_URL + "/upload/" + testCollectionName, RequestOptions.create().setMultipart(formData)
                    .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
            );
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to load test file from resources", ex);
        }
    }

    public APIResponse delete(String testCollectionName) {
        return apiRequestContext.delete(FILE_BASE_URL + "/delete/" + testCollectionName + "/testUploadFile.txt", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getAllFiles(String testCollectionName) {
        return apiRequestContext.get(FILE_BASE_URL + "/get-all/" + testCollectionName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse downloadFile(String testCollectionName, String fileName) {
        return apiRequestContext.get(FILE_BASE_URL + "/download/" + testCollectionName + "/" + fileName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse streamFile(String testCollectionName, String fileName) {
        return apiRequestContext.get(FILE_BASE_URL + "/stream/" + testCollectionName + "/" + fileName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }
    // Integration function start: Auth
    public APIResponse getCurrentUserRole(String testCollectionName) {
        return apiRequestContext.get(FILE_BASE_URL + "/get-current-user-role/" + testCollectionName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getUsersByCollection(String testCollectionName) {
        return apiRequestContext.get(FILE_BASE_URL + "/get-users-by-collection/" + testCollectionName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getAllRoles() {
        return apiRequestContext.get(FILE_BASE_URL + "/get-all-roles", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse addUserToCollection(String testCollectionName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("collectionName", testCollectionName);
        requestBody.put("username", testUsername);
        requestBody.put("role", "READ_WRITE");

        return apiRequestContext.post(FILE_BASE_URL + "/add-user-to-collection", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse updateUserRole(String testCollectionName, String newRole) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("collectionName", testCollectionName);
        requestBody.put("username", testUsername);
        requestBody.put("role", newRole);

        return apiRequestContext.post(FILE_BASE_URL + "/update-user-role", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteUserFromCollection(String testCollectionName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("collectionName", testCollectionName);
        requestBody.put("username", testUsername);

        return apiRequestContext.post(FILE_BASE_URL + "/delete-user-from-collection", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse deleteUserFromAllCollections() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", testUsername);

        return apiRequestContext.post(FILE_BASE_URL + "/delete-user-from-all-collections", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }// Integration function end: Auth
}
