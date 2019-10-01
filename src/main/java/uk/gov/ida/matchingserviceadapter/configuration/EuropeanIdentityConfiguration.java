package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EuropeanIdentityConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private String hubConnectorEntityId;

    @Valid
    @JsonProperty
    private String[] acceptableHubConnectorEntityIds;

    @NotNull
    @Valid
    @JsonProperty
    private boolean enabled;

    @Valid
    @JsonProperty
    private EidasMetadataConfiguration aggregatedMetadata;

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    public String[] getAcceptableHubConnectorEntityIds() {
        return acceptableHubConnectorEntityIds != null && acceptableHubConnectorEntityIds.length > 0
            ? acceptableHubConnectorEntityIds
            : new String[] { hubConnectorEntityId };
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
