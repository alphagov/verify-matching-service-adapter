package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.ida.matchingserviceadapter.configuration.EuropeanConfigurationConstants.getDefaultAcceptableHubConnectorIds;

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

    public List<String> getAcceptableHubConnectorEntityIds(MatchingServiceAdapterEnvironment environment) {
        Set<String> entityIds = new HashSet<>(getDefaultAcceptableHubConnectorIds().getOrDefault(environment, new ArrayList<>()));
        Optional.ofNullable(acceptableHubConnectorEntityIds).ifPresent(entityIds::addAll);
        Optional.ofNullable(hubConnectorEntityId).ifPresent(entityIds::add);
        return new ArrayList<>(entityIds);
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
