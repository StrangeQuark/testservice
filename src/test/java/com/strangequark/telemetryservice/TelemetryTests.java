// Integration file: Telemetry

package com.strangequark.telemetryservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.strangequark.utility.ExtentTestWatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
        APIResponse response = telemetryFunctions.getEventsWithoutAccess();

        assertEquals(401, response.status());
    }
    // Integration function end: Auth
}
