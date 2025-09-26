package com.strangequark.fileservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.strangequark.authservice.AuthFunctions;
import com.strangequark.utility.AuthUtility;

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
}
