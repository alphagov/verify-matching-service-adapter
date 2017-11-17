package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;

public class HealthCheckMatchingServiceResponse implements MatchingServiceResponse {

    private final HealthCheckResponseFromMatchingService healthCheckResponseFromMatchingService;

    public HealthCheckMatchingServiceResponse(HealthCheckResponseFromMatchingService healthCheckResponseFromMatchingService) {
        this.healthCheckResponseFromMatchingService = healthCheckResponseFromMatchingService;
    }

    public HealthCheckResponseFromMatchingService getHealthCheckResponseFromMatchingService() {
        return healthCheckResponseFromMatchingService;
    }
}
