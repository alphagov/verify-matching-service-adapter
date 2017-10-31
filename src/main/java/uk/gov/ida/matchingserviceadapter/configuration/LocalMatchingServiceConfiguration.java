package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

import static uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration.getDefaultJerseyClientConfiguration;

public class LocalMatchingServiceConfiguration {
    @NotNull
    @Valid
    @JsonProperty
    private URI matchUrl;

    @NotNull
    @Valid
    @JsonProperty
    private URI accountCreationUrl;

    @NotNull
    @Valid
    @JsonProperty
    private JerseyClientConfiguration client = getDefaultJerseyClientConfiguration(false, true);

    protected LocalMatchingServiceConfiguration() {}

    public URI getMatchUrl() {
        return matchUrl;
    }

    public URI getAccountCreationUrl() {
        return accountCreationUrl;
    }

    public JerseyClientConfiguration getClient() {
        return client;
    }
}
