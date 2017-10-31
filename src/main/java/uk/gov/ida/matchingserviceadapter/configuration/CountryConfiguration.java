package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CountryConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    public String hubConnectorEntityId;

    @NotNull
    @Valid
    @JsonProperty
    private TrustStoreBackedMetadataConfiguration metadata;

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    public MetadataResolverConfiguration getMetadata() {
        return metadata;
    }
}
