package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class HubConfiguration {
    @NotNull
    @Valid
    @JsonProperty
    private URI ssoUrl;

    @NotNull
    @Valid
    @JsonProperty
    private Boolean republishHubCertificatesInLocalMetadata = false;

    @NotNull
    @Valid
    @JsonProperty
    private TrustStoreConfiguration trustStore;

    @NotNull
    @Valid
    @JsonProperty
    private String hubEntityId = "https://signin.service.gov.uk";

    protected HubConfiguration() {}

    public URI getSsoUrl() {
        return ssoUrl;
    }

    public Boolean getRepublishHubCertificatesInLocalMetadata() {
        return republishHubCertificatesInLocalMetadata;
    }

    public TrustStoreConfiguration getTrustStore() {
        return trustStore;
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

}
