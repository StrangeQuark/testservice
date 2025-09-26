package com.strangequark.fileservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions;
import com.strangequark.utility.AuthUtility;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public APIResponse upload(String testCollectionName) {
        try {
            Path filePath = Paths.get(getClass().getClassLoader().getResource("fileserviceTestFiles/testUploadFile.txt").toURI());
            FormData formData = FormData.create().set("file", filePath);

            return apiRequestContext.post(FILE_BASE_URL + "/upload/" + testCollectionName, RequestOptions.create().setMultipart(formData)
                    .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
            );
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Failed to load test file from resources", ex);
        }
    }
}
