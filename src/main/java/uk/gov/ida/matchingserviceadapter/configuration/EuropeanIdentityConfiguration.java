package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EuropeanIdentityConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private String hubConnectorEntityId;

    @NotNull
    @Valid
    @JsonProperty
    private boolean enabled;

    @NotNull
    @Valid
    @JsonProperty
    private TrustStoreBackedMetadataConfiguration metadata;

    @Valid
    @JsonProperty
    private EidasMetadataConfiguration aggregatedMetadata;

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    public MetadataResolverConfiguration getMetadata() {
        return metadata;
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
