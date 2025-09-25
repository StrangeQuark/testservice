// Integration file: Email

package com.strangequark.emailservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth

import java.util.HashMap;
import java.util.Map;

public class EmailFunctions {
    APIRequestContext apiRequestContext;
    AuthFunctions authFunctions; // Integration line: Auth

    private static final String EMAIL_BASE_URL = "http://localhost:6005/api/email";

    public EmailFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
        this.authFunctions = new AuthFunctions(apiRequestContext); // Integration line: Auth
    }
    // Integration function start: Auth
    public EmailFunctions(APIRequestContext apiRequestContext, AuthFunctions authFunctions) {
        this.apiRequestContext = apiRequestContext;
        this.authFunctions = authFunctions;
    } // Integration function end: Auth

    public APIResponse healthcheck() {
        return apiRequestContext.get(EMAIL_BASE_URL + "/health");
    }

    public APIResponse sendEmail(String recipient, String sender, String email, String subject) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("email", email);
        requestBody.put("subject", subject);
        String accessToken = authFunctions.registerEnableAuthenticateAccess(EmailTests.testUsername, EmailTests.testEmail, EmailTests.testPassword); // Integration line: Auth

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken) // Integration line: Auth
        );
    }

    public APIResponse sendEmailWithToken(String recipient, String sender, String email, String subject) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("email", email);
        requestBody.put("subject", subject);
        String accessToken = authFunctions.registerEnableAuthenticateAccess(EmailTests.testUsername, EmailTests.testEmail, EmailTests.testPassword); // Integration line: Auth

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-email-with-token", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken) // Integration line: Auth
        );
    }

    public APIResponse sendRegisterEmail(String recipient, String sender, String subject) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("subject", subject);
        String accessToken = authFunctions.registerEnableAuthenticateAccess(EmailTests.testUsername, EmailTests.testEmail, EmailTests.testPassword); // Integration line: Auth

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-register-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken) // Integration line: Auth
        );
    }

    public APIResponse sendPasswordResetEmail(String recipient, String sender, String subject) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("subject", subject);
        String accessToken = authFunctions.registerEnableAuthenticateAccess(EmailTests.testUsername, EmailTests.testEmail, EmailTests.testPassword); // Integration line: Auth

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-password-reset-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + accessToken) // Integration line: Auth
        );
    }

    public APIResponse confirmToken() {
        return apiRequestContext.get(EMAIL_BASE_URL + "/confirm-token");
    }
    // Integration function start: Auth
    public APIResponse enableUser() {
        return apiRequestContext.get(EMAIL_BASE_URL + "/enable-user");
    }// Integration function end: Auth
}
