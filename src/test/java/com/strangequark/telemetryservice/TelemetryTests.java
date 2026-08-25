// Integration file: Telemetry

package com.strangequark.telemetryservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.authservice.AuthFunctions; // Integration line: Auth
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ExtentTestWatcher.class)
public class TelemetryTests {
    private static Playwright playwright;
    private static APIRequestContext apiRequestContext;
    private static TelemetryFunctions telemetryFunctions;

    @BeforeAll
    public static void beforeAll() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext();
        telemetryFunctions = new TelemetryFunctions(apiRequestContext);
    }

    @Test
    public void healthcheckTest() {
        APIResponse response = telemetryFunctions.healthcheck();

        assertTrue(response.ok(), "Telemetry service healthcheck failed: " + response.status() + " - " + response.text());
    }

    // Integration function start: Auth
    @Test
    public void unauthenticatedGetEventsTest() {
        APIRequestContext unauthenticatedRequestContext = playwright.request().newContext();
        TelemetryFunctions unauthenticatedTelemetryFunctions = new TelemetryFunctions(unauthenticatedRequestContext);
        APIResponse response = unauthenticatedTelemetryFunctions.getEventsWithoutAccess();

        assertEquals(401, response.status());
        unauthenticatedRequestContext.dispose();
    }

    @Test
    public void serviceAccountCanGetEventsTest() {
        AuthFunctions authFunctions = new AuthFunctions(apiRequestContext);
        String accessToken = authFunctions.extractJwt(authFunctions.serviceAccountAuthenticate("test"));

        APIResponse response = telemetryFunctions.getEvents(accessToken);

        assertTrue(response.ok(), "Test service account should access TelemetryService: " +
                response.status() + " - " + response.text());
    }

    @Test
    public void normalUserCannotGetEventsTest() {
        AuthFunctions authFunctions = new AuthFunctions(apiRequestContext);
        String username = "test_" + UUID.randomUUID();
        String email = username + "@email.com";
        String password = UUID.randomUUID().toString();
        String accessToken = authFunctions.registerEnableAuthenticateAccess(username, email, password);

        APIResponse response = telemetryFunctions.getEvents(accessToken);

        assertEquals(403, response.status());

        response = authFunctions.deleteUser(username, email, password);
        assertTrue(response.ok(), "User cleanup failed: " + response.status() + " - " + response.text());
    }
    // Integration function end: Auth
}
