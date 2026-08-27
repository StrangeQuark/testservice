// Integration file: Telemetry

package com.strangequark.telemetryservice;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.utility.EnvUtility;

public class TelemetryFunctions {
    public static final String TELEMETRY_BASE_URL = EnvUtility.getEnvVar("TELEMETRY_BASE_URL");

    private final APIRequestContext apiRequestContext;

    public TelemetryFunctions(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;
    }

    public APIResponse healthcheck() {
        return apiRequestContext.get(TELEMETRY_BASE_URL + "/health");
    }

    public APIResponse getEventsWithoutAccess() {
        return apiRequestContext.get(TELEMETRY_BASE_URL + "/get-events?eventType=test");
    }

    public APIResponse getEvents(String accessToken) {
        return apiRequestContext.get(TELEMETRY_BASE_URL + "/get-events?eventType=test", RequestOptions.create()
                .setHeader("Authorization", "Bearer " + accessToken)
        );
    }
}
