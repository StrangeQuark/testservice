package com.strangequark.gatewayservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.EnvUtility;
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExtentTestWatcher.class)
@EnabledIfEnvironmentVariable(named = "AUTHSERVICE_INTEGRATION", matches = "true")
@EnabledIfEnvironmentVariable(named = "GATEWAYSERVICE_INTEGRATION", matches = "true")
public class GatewayTracingTests {
    @Test
    public void traceContinuesThroughGatewayToAuthTest() {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        try(Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            try {
                APIResponse response = request.get(EnvUtility.getEnvVar("GATEWAY_BASE_URL") + "/api/auth/health",
                        RequestOptions.create().setHeader("traceparent", "00-" + traceId + "-00f067aa0ba902b7-01"));
                assertEquals(200, response.status());
                assertEquals(traceId, response.headers().get("x-trace-id"));

                APIResponse rejected = request.post(EnvUtility.getEnvVar("AUTH_BASE_URL") + "/access",
                        RequestOptions.create().setHeader("traceparent", "00-" + traceId + "-00f067aa0ba902b7-01")
                                .setData("{}").setHeader("Content-Type", "application/json"));
                assertTrue(rejected.status() >= 400);
                assertEquals(traceId, rejected.headers().get("x-trace-id"));

                APIResponse newRequest = request.get(EnvUtility.getEnvVar("AUTH_BASE_URL") + "/health");
                assertEquals(200, newRequest.status());
                String newTraceId = newRequest.headers().get("x-trace-id");
                assertNotNull(newTraceId);
                assertTrue(newTraceId.matches("[0-9a-f]{32}"));
                assertNotEquals(traceId, newTraceId);
            } finally {
                request.dispose();
            }
        }
    }
}
