// Integration file: Gateway

package com.strangequark.gatewayservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.EnvUtility;
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ExtentTestWatcher.class)
@Tag("rate-limit")
public class GatewayRateLimitTests {
    private static final String GATEWAY_BASE_URL = EnvUtility.getEnvVar("GATEWAY_BASE_URL");
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
    }

    @AfterAll
    public static void afterAll() {
        apiRequestContext.dispose();
        playwright.close();
    }

    @Test
    public void loginRateLimitTest() {
        assertRateLimit("/api/auth/authenticate", 11);
    }

    @Test
    public void registerRateLimitTest() {
        assertRateLimit("/api/auth/register", 3);
    }

    @Test
    public void sendPasswordResetEmailRateLimitTest() {
        assertRateLimit("/api/auth/user/send-password-reset-email", 4);
    }

    @Test
    public void resetPasswordRateLimitTest() {
        assertRateLimit("/api/auth/user/reset-password", 4);
    }

    private void assertRateLimit(String endpoint, int attempts) {
        APIResponse response = null;

        for(int i = 0; i < attempts; i++) {
            response = apiRequestContext.post(GATEWAY_BASE_URL + endpoint,
                    RequestOptions.create().setData(new HashMap<>()));
        }

        assertEquals(429, response.status(), endpoint + " should return 429 after reaching the rate limit");
    }
}
