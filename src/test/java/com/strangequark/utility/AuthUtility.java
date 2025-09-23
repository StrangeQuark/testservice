package com.strangequark.utility;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

public class AuthUtility {
    private Playwright playwright;
    private APIRequestContext apiRequestContext;
    private final String SERVICE_SECRET_TEST;
    private final String AUTH_BOOTSTRAP_SECRET_KEY;

    private final String BASE_URL = "http://localhost:6001/api/auth";

    public AuthUtility() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();

        // Attempt to get the TestService service secret for auth integrations
        String serviceSecret = System.getenv("SERVICE_SECRET_TEST");
        if (serviceSecret == null || serviceSecret.isEmpty()) {
            Dotenv dotenv = Dotenv.load();
            serviceSecret = dotenv.get("SERVICE_SECRET_TEST");
        }
        if (serviceSecret == null || serviceSecret.isEmpty()) {
            throw new IllegalStateException("SERVICE_SECRET_TEST is not set in environment variables or .env file.");
        }
        SERVICE_SECRET_TEST = serviceSecret;

        // Attempt to get the AuthService bootstrap secret key for bootstrapSuperUserTest
        String bootstrapSecret = System.getenv("AUTH_BOOTSTRAP_SECRET_KEY");
        if (bootstrapSecret == null || bootstrapSecret.isEmpty()) {
            Dotenv dotenv = Dotenv.load();
            bootstrapSecret = dotenv.get("AUTH_BOOTSTRAP_SECRET_KEY");
        }
        if (bootstrapSecret == null || bootstrapSecret.isEmpty()) {
            throw new IllegalStateException("AUTH_BOOTSTRAP_SECRET_KEY is not set in environment variables or .env file.");
        }

        AUTH_BOOTSTRAP_SECRET_KEY = bootstrapSecret;
    }

    public String authenticateServiceAccount() {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("clientId", "test");
            requestBody.put("clientPassword", SERVICE_SECRET_TEST);

            APIResponse response = apiRequestContext.post(BASE_URL + "/service-account/authenticate",
                    RequestOptions.create().setData(requestBody));

            String res = response.text().replace("\"", "");
            res = res.replace("}", "");

            if(!res.contains("jwtToken"))
                throw new RuntimeException("jwtToken not found in authentication response");

            return res.substring(res.indexOf("jwtToken:") + 9).trim();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String getAuthBootstrapSecretKey() {
        return AUTH_BOOTSTRAP_SECRET_KEY;
    }

    public String getServiceSecretTest() {
        return SERVICE_SECRET_TEST;
    }
}

