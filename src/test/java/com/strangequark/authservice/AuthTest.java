package com.strangequark.authservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    private static Playwright playwright;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
    }

    @Test
    public void registerTest() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "test");
        requestBody.put("email", "email@email.com");
        requestBody.put("password", "password123!");

        APIResponse response = playwright.request().newContext()
                .post("http://localhost:6001/auth/register", RequestOptions.create().setData(requestBody));

        assertTrue(response.ok());
    }

    @Test
    public void registerAndEnableUser() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "test2");
        requestBody.put("email", "email2@email.com");
        requestBody.put("password", "password123!");

        APIResponse registerResponse = playwright.request().newContext()
                .post("http://localhost:6001/auth/register", RequestOptions.create().setData(requestBody));

        JsonObject jsonResponse = JsonParser.parseString(registerResponse.text()).getAsJsonObject();
        String jwtToken = String.valueOf(jsonResponse.get("jwtToken"));

        requestBody.remove("username");
        requestBody.remove("password");

        APIResponse enableUserResponse = playwright.request().newContext(new APIRequest.NewContextOptions().setExtraHTTPHeaders(
                        (Map<String, String>) new HashMap<>().put("Authorization", "Bearer " + jwtToken)
                ))
                .post("http://localhost:6001/user/enableUser", RequestOptions.create().setData(requestBody));

        assertTrue(enableUserResponse.ok());
    }
}
