// Integration file: Email

package com.strangequark.emailservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import com.strangequark.utility.AuthUtility; // Integration line: Auth
import com.strangequark.utility.EnvUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // Integration line: Auth

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailFunctions {
    APIRequestContext apiRequestContext;
    AuthFunctions authFunctions; // Integration function start: Auth
    AuthUtility authUtility;

    public String testUsername;
    public String testEmail;
    public String testPassword;// Integration function end: Auth

    public static final String EMAIL_BASE_URL = EnvUtility.getEnvVar("EMAIL_BASE_URL");

    public EmailFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
    }
    // Integration function start: Auth
    public EmailFunctions(APIRequestContext apiRequestContext, AuthFunctions authFunctions) {
        this(apiRequestContext);
        this.authFunctions = authFunctions;
        this.authUtility = new AuthUtility(apiRequestContext); // Integration line: Auth
    } // Integration function end: Auth

    public APIResponse healthcheck() {
        return apiRequestContext.get(EMAIL_BASE_URL + "/health");
    }

    public APIResponse sendEmail(String recipient, String sender, String body, String subject) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("body", body);
        requestBody.put("subject", subject);

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse getTemplateEmail(String templateName) {
        return apiRequestContext.get(EMAIL_BASE_URL + "/get-template-email?templateName=" + templateName, RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse sendTemplateEmail(String recipient, String sender, boolean includeToken, String templateName,
                                         Map<String, String> templateVariables) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("includeToken", includeToken);
        requestBody.put("templateName", templateName);
        requestBody.put("templateVariables", templateVariables);

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-template-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse createTemplateEmail(String body, String subject, String templateName) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("body", body);
        requestBody.put("subject", subject);
        requestBody.put("templateName", templateName);

        return apiRequestContext.post(EMAIL_BASE_URL + "/create-template-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse sendEmailWithToken(String recipient, String sender, String body, String subject) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("body", body);
        requestBody.put("subject", subject);
        requestBody.put("includeToken", true);

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse sendRegisterEmail(String recipient, String sender) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("includeToken", true);
        requestBody.put("templateName", "USER_REGISTER");
        requestBody.put("templateVariables", Map.of("link", "http://react-service/confirm-email"));

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-template-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse sendPasswordResetEmail(String recipient, String sender) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", sender);
        requestBody.put("includeToken", true);
        requestBody.put("templateName", "USER_PASSWORD_RESET");
        requestBody.put("templateVariables", Map.of("link", "http://react-service/new-password"));

        return apiRequestContext.post(EMAIL_BASE_URL + "/send-template-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }

    public APIResponse confirmToken() {
        APIResponse response = sendEmailWithToken("recipient@email.com", "sender@email.com",
                "Test email", "Test subject");
        assertTrue(response.ok(), "Send email with token test failed: " + response.status() + " - " + response.text());
        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();

        return apiRequestContext.get(EMAIL_BASE_URL + "/confirm-token?token=" + jsonObject.get("token").getAsString()
            , RequestOptions.create().setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()) // Integration line: Auth
        );
    }
    // Integration function start: Auth
    public APIResponse enableUser() {
        testUsername = "test_" + UUID.randomUUID();
        testEmail = testUsername + "@email.com";
        testPassword = UUID.randomUUID().toString();

        APIResponse response = authFunctions.register(testUsername, testEmail, testPassword);
        assertTrue(response.ok(), "Register user in Email enable user test failed: " + response.status() + " - " + response.text());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", testEmail);
        requestBody.put("sender", "sender@email.com");
        requestBody.put("includeToken", true);
        requestBody.put("templateName", "USER_REGISTER");
        requestBody.put("templateVariables", Map.of("link", "http://react-service/confirm-email"));

        response = apiRequestContext.post(EmailFunctions.EMAIL_BASE_URL + "/send-template-email", RequestOptions.create().setData(requestBody)
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()));
        assertTrue(response.ok(), "Send register email test failed: " + response.status() + " - " + response.text());

        JsonObject jsonObject = JsonParser.parseString(response.text()).getAsJsonObject();
        return apiRequestContext.get(EMAIL_BASE_URL + "/enable-user?token=" + jsonObject.get("token").getAsString(), RequestOptions.create()
                .setHeader("Authorization", "Bearer " + authUtility.authenticateServiceAccount()));
    }// Integration function end: Auth
}
