package uk.gov.ida.matchingserviceadapter.configuration;

import io.dropwizard.util.Duration;

public interface AssertionLifetimeConfiguration {
    Duration getAssertionLifetime();
}
