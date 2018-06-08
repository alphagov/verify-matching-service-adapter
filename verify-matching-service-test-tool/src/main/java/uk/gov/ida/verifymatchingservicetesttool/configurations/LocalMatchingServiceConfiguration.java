package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @Valid
    @JsonProperty
    private boolean usesUniversalDataset;

    @JsonCreator
    public LocalMatchingServiceConfiguration(
        @JsonProperty("matchUrl") URI matchUrl,
        @JsonProperty("accountCreationUrl") URI accountCreationUrl,
        @JsonProperty("usesUniversalDataset") boolean usesUniversalDataset
    ) {
        this.matchUrl = matchUrl;
        this.accountCreationUrl = accountCreationUrl;
        this.usesUniversalDataset = usesUniversalDataset;
    }

    public URI getMatchUrl() {
        return matchUrl;
    }

    public URI getAccountCreationUrl() {
        return accountCreationUrl;
    }

    public Boolean usesUniversalDataset() {
        return usesUniversalDataset;
    }
}
