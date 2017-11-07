package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class ApplicationConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private LocalMatchingServiceConfiguration localMatchingService;

    protected ApplicationConfiguration() {
    }

    public URI getLocalMatchingServiceMatchUrl() {
        return localMatchingService.getMatchUrl();
    }

    public URI getLocalMatchingServiceAccountCreationUrl() {
        return localMatchingService.getAccountCreationUrl();
    }
}
