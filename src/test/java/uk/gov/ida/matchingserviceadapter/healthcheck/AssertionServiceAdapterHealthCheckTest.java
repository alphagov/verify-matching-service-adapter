package uk.gov.ida.matchingserviceadapter.healthcheck;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AssertionServiceAdapterHealthCheckTest {

    private MatchingServiceAdapterHealthCheck healthCheck = new MatchingServiceAdapterHealthCheck();

    @Test
    public void shouldReturnHealthy() {
        assertThat(healthCheck.getName()).isEqualTo("Matching Service Adapter Health Check");
        assertThat(healthCheck.check().isHealthy()).isTrue();
    }

}