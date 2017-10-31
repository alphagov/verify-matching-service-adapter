package uk.gov.ida.matchingserviceadapter.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class MatchingServiceAdapterHealthCheck extends HealthCheck {
    public String getName() {
        return "Matching Service Adapter Health Check";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
