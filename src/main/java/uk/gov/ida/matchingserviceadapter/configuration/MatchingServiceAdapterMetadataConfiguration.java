package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.matchingserviceadapter.exceptions.UninitialisedKeyStoreException;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.metadata.exception.EmptyTrustStoreException;

import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Optional;

public class MatchingServiceAdapterMetadataConfiguration extends TrustStoreBackedMetadataConfiguration {

    private final MatchingServiceAdapterEnvironment environment;

    private final TrustStoreConfiguration hubTrustStore;

    private final TrustStoreConfiguration idpTrustStore;

    private TrustStoreConfiguration metadataTrustStore;

    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public MatchingServiceAdapterMetadataConfiguration(
        @JsonProperty("uri") @JsonAlias({ "url" }) URI uri,
        @JsonProperty("minRefreshDelay") Long minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("client") JerseyClientConfiguration client,
        @JsonProperty("jerseyClientName") String jerseyClientName,
        @JsonProperty("hubFederationId") String hubFederationId,
        @JsonProperty("trustStore") TrustStoreConfiguration metadataTrustStore,
        @JsonProperty("hubTrustStore") TrustStoreConfiguration hubTrustStore,
        @JsonProperty("idpTrustStore") TrustStoreConfiguration idpTrustStore,
        @JsonProperty("environment") MatchingServiceAdapterEnvironment environment) {

        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, jerseyClientName, hubFederationId, metadataTrustStore);

        if ((hubTrustStore == null || idpTrustStore == null || metadataTrustStore == null) && environment == null) {
            throw new IllegalArgumentException(
                    "Missing property 'environment' in the 'metadata' section of the config: set environment to either PRODUCTION or INTEGRATION to use the default 'hub', 'idp' or 'metadata' truststores, " +
                            "or override all by providing configuration for 'hubTrustStore', 'idpTrustStore' and 'trustStore'");
        }

        this.metadataTrustStore = metadataTrustStore;
        this.hubTrustStore = hubTrustStore;
        this.idpTrustStore = idpTrustStore;
        this.environment = Optional.ofNullable(environment).orElse(MatchingServiceAdapterEnvironment.INTEGRATION);
    }

    public MatchingServiceAdapterEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public KeyStore getTrustStore() {
        return metadataTrustStore == null ?
                new DefaultTrustStoreConfiguration(environment, TrustStoreType.METADATA).getTrustStore() :
                super.getTrustStore();
    }

    @Override
    public Optional<KeyStore> getHubTrustStore() {
        return Optional.of(
            validateTruststore(
                Optional.ofNullable(hubTrustStore)
                        .orElseGet(() -> new DefaultTrustStoreConfiguration(environment, TrustStoreType.HUB)).getTrustStore()));
    }

    @Override
    public Optional<KeyStore> getIdpTrustStore() {
        return Optional.of(
            validateTruststore(
                Optional.ofNullable(idpTrustStore)
                        .orElseGet(() -> new DefaultTrustStoreConfiguration(environment, TrustStoreType.IDP)).getTrustStore()));
    }

    private KeyStore validateTruststore(KeyStore trustStore) {
        try {
            final int trustStoreSize = trustStore.size();
            if (trustStoreSize == 0) {
                throw new EmptyTrustStoreException();
            }
            return trustStore;
        } catch (KeyStoreException e) {
            throw new UninitialisedKeyStoreException(e);
        }
    }
}
