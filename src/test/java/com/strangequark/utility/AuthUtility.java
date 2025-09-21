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

    private final String BASE_URL = "http://localhost:6001/api/auth";

    public AuthUtility() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();

        String secret = System.getenv("SERVICE_SECRET_TEST");

        if (secret == null || secret.isEmpty()) {
            Dotenv dotenv = Dotenv.load();
            secret = dotenv.get("SERVICE_SECRET_TEST");
        }

        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("SERVICE_SECRET_TEST is not set in environment variables or .env file.");
        }

        SERVICE_SECRET_TEST = secret;
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
}

