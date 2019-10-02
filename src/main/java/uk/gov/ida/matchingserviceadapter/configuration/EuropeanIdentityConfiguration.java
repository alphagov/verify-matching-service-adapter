package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EuropeanIdentityConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private String hubConnectorEntityId;

    @Valid
    @JsonProperty
    private List<String> acceptableHubConnectorEntityIds;

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

    public List<String> getAcceptableHubConnectorEntityIds() {
        Set<String> entityIds = new LinkedHashSet<>();
        Optional.ofNullable(acceptableHubConnectorEntityIds).ifPresent(entityIds::addAll);
        Optional.ofNullable(hubConnectorEntityId).ifPresent(entityIds::add);
        return new LinkedList<>(entityIds);
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
