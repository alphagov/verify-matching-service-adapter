package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalMatchingServiceConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private URI matchUrl;

    @NotNull
    @Valid
    @JsonProperty
    private URI accountCreationUrl;

    protected LocalMatchingServiceConfiguration() {}

    public URI getMatchUrl() {
        return matchUrl;
    }

    public URI getAccountCreationUrl() {
        return accountCreationUrl;
    }
}
