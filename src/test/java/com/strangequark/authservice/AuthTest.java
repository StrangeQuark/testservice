package com.strangequark.authservice;

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

        requestBody.remove("username");
        requestBody.remove("password");

        APIResponse enableUserResponse = playwright.request().newContext()
                .post("http://localhost:6001/user/enableUser", RequestOptions.create().setData(requestBody));

        assertTrue(enableUserResponse.ok());
    }

    @Test
    public void registerEnableAndAuthenticateUser() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "test3");
        requestBody.put("email", "email3@email.com");
        requestBody.put("password", "password123!");

        APIResponse registerResponse = playwright.request().newContext()
                .post("http://localhost:6001/auth/register", RequestOptions.create().setData(requestBody));

        requestBody.remove("username");
        requestBody.remove("password");

        APIResponse enableUserResponse = playwright.request().newContext()
                .post("http://localhost:6001/user/enableUser", RequestOptions.create().setData(requestBody));

        requestBody.remove("email");
        requestBody.put("username", "test3");
        requestBody.put("password", "password123!");

        APIResponse authenticationResponse = playwright.request().newContext()
                .post("http://localhost:6001/auth/authenticate", RequestOptions.create().setData(requestBody));

        assertTrue(authenticationResponse.ok());
    }
}
