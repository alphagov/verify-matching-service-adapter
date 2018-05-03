package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
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
        @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
        @JsonProperty("hubTrustStore") TrustStoreConfiguration hubTrustStore,
        @JsonProperty("idpTrustStore") TrustStoreConfiguration idpTrustStore,
        @JsonProperty("environment") MatchingServiceAdapterEnvironment environment) {

        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, jerseyClientName, hubFederationId, trustStore);
        this.hubTrustStore = hubTrustStore;
        this.idpTrustStore = idpTrustStore;
        this.environment = Optional.ofNullable(environment).orElse(MatchingServiceAdapterEnvironment.INTEGRATION);
    }

    @Override
    public Optional<KeyStore> getHubTrustStore() {
        return Optional.of(
            validateTruststore(
                Optional.ofNullable(hubTrustStore)
                        .orElseGet(() -> new DefaultHubTrustStoreConfiguration(environment)).getTrustStore()));
    }

    @Override
    public Optional<KeyStore> getIdpTrustStore() {
        return Optional.of(
            validateTruststore(
                Optional.ofNullable(idpTrustStore)
                        .orElseGet(() -> new DefaultIdentityProviderTrustStoreConfiguration(environment)).getTrustStore()));
    }

    private KeyStore validateTruststore(KeyStore trustStore) {
        int trustStoreSize;
        try {
            trustStoreSize = trustStore.size();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        if (trustStoreSize == 0) {
            throw new EmptyTrustStoreException();
        }
        return trustStore;
    }
}
