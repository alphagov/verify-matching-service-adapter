package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class ServiceInfo {
    @NotNull
    @Valid
    @JsonProperty
    private String name = "matching-service-adapter";

    @NotNull
    @Valid
    @JsonProperty
    private String entityId;

    @NotNull
    @Valid
    @JsonProperty
    private URI externalUrl;

    @NotNull
    @Valid
    @JsonProperty
    private Duration assertionLifetime = Duration.minutes(60);

    protected ServiceInfo() {}

    public String getName() {
        return name;
    }

    public String getEntityId() {
        return entityId;
    }

    public URI getExternalUrl() {
        return externalUrl;
    }

    public Duration getAssertionLifetime() {
        return assertionLifetime;
    }
}
