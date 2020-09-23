package uk.gov.ida.matchingserviceadapter.configuration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_EIDAS_METADATA_SOURCE_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_METADATA_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_TRUST_ANCHOR_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_EIDAS_METADATA_SOURCE_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_METADATA_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_TRUST_ANCHOR_URI;


public enum MatchingServiceAdapterEnvironment {
    PRODUCTION(
            PRODUCTION_HUB_TRUSTSTORE_NAME,
            PRODUCTION_IDP_TRUSTSTORE_NAME,
            PRODUCTION_METADATA_TRUSTSTORE_NAME,
            PRODUCTION_TRUST_ANCHOR_URI,
            PRODUCTION_EIDAS_METADATA_SOURCE_URI
    ),
    INTEGRATION(
            INTEGRATION_HUB_TRUSTSTORE_NAME,
            INTEGRATION_IDP_TRUSTSTORE_NAME,
            INTEGRATION_METADATA_TRUSTSTORE_NAME,
            INTEGRATION_TRUST_ANCHOR_URI,
            INTEGRATION_EIDAS_METADATA_SOURCE_URI
    );

    private Map<TrustStoreType, String> trustStoreNames = new HashMap<>();
    private URI trustAnchorUri;
    private URI eidasMetadataSourceUri;

    public String getTrustStoreName(TrustStoreType trustStoreType) {
        return trustStoreNames.get(trustStoreType);
    }

    public URI getTrustAnchorUri() { return trustAnchorUri; }

    public URI getMetadataSourceUri() { return eidasMetadataSourceUri; }

    MatchingServiceAdapterEnvironment(
            String hubTrustStoreName,
            String idpTrustStoreName,
            String metadataTrustStoreName,
            URI trustAnchorUri,
            URI eidasMetadataSourceUri) {
        trustStoreNames.put(TrustStoreType.HUB, hubTrustStoreName);
        trustStoreNames.put(TrustStoreType.IDP, idpTrustStoreName);
        trustStoreNames.put(TrustStoreType.METADATA, metadataTrustStoreName);
        this.trustAnchorUri = trustAnchorUri;
        this.eidasMetadataSourceUri = eidasMetadataSourceUri;
    }

}
