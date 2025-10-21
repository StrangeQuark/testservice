// Integration file: Auth

package com.strangequark.utility;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.strangequark.authservice.AuthFunctions;

import java.util.HashMap;
import java.util.Map;

public class AuthUtility {
    private final APIRequestContext apiRequestContext;
    private final String SERVICE_SECRET_TEST;
    private final String AUTH_BOOTSTRAP_SECRET_KEY;

    public AuthUtility(APIRequestContext apiRequestContext) {
        this.apiRequestContext = apiRequestContext;

        // Attempt to get the TestService service secret for auth integrations
        SERVICE_SECRET_TEST = EnvUtility.getEnvVar("SERVICE_SECRET_TEST");

        // Attempt to get the AuthService bootstrap secret key for bootstrapSuperUserTest
        AUTH_BOOTSTRAP_SECRET_KEY = EnvUtility.getEnvVar("AUTH_BOOTSTRAP_SECRET_KEY");
    }

    public String authenticateServiceAccount() {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("clientId", "test");
            requestBody.put("clientPassword", SERVICE_SECRET_TEST);

            APIResponse response = apiRequestContext.post(AuthFunctions.AUTH_BASE_URL + "/service-account/authenticate",
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

    public String getServiceSecretTest(String clientId) {
        return SERVICE_SECRET_TEST;
    }
}

