package uk.gov.ida.matchingserviceadapter.domain;

public class HealthCheckMatchingServiceResponse implements MatchingServiceResponse {

    private final HealthCheckResponseFromMatchingService healthCheckResponseFromMatchingService;

    public HealthCheckMatchingServiceResponse(HealthCheckResponseFromMatchingService healthCheckResponseFromMatchingService) {
        this.healthCheckResponseFromMatchingService = healthCheckResponseFromMatchingService;
    }

    public HealthCheckResponseFromMatchingService getHealthCheckResponseFromMatchingService() {
        return healthCheckResponseFromMatchingService;
    }
}
