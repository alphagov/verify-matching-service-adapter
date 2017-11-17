package uk.gov.ida.matchingserviceadapter.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MatchingServiceAdapterHealthCheckTest {

    private MatchingServiceAdapterHealthCheck healthCheck = new MatchingServiceAdapterHealthCheck();

    @Test
    public void shouldReturnHealthy() {
        assertThat(healthCheck.getName()).isEqualTo("Matching Service Adapter Health Check");
        assertThat(healthCheck.check().isHealthy()).isTrue();
    }

}