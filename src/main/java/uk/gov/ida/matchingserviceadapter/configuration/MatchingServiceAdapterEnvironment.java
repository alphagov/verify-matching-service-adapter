package uk.gov.ida.matchingserviceadapter.configuration;

import java.util.HashMap;

import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_METADATA_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_METADATA_TRUSTSTORE_NAME;

public enum MatchingServiceAdapterEnvironment {
    PRODUCTION(PRODUCTION_HUB_TRUSTSTORE_NAME, PRODUCTION_IDP_TRUSTSTORE_NAME, PRODUCTION_METADATA_TRUSTSTORE_NAME),
    INTEGRATION(INTEGRATION_HUB_TRUSTSTORE_NAME, INTEGRATION_IDP_TRUSTSTORE_NAME, INTEGRATION_METADATA_TRUSTSTORE_NAME);

    private HashMap<TrustStoreType, String> trustStoreNames = new HashMap<>();

    public String getTrustStoreName(TrustStoreType trustStoreType) {
        return trustStoreNames.get(trustStoreType);
    }

    MatchingServiceAdapterEnvironment(
            String hubTrustStoreName,
            String idpTrustStoreName,
            String metadataTrustStoreName) {
        trustStoreNames.put(TrustStoreType.HUB, hubTrustStoreName);
        trustStoreNames.put(TrustStoreType.IDP, idpTrustStoreName);
        trustStoreNames.put(TrustStoreType.METADATA, metadataTrustStoreName);
    }

}
