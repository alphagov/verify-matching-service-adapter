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

        if ((hubTrustStore == null || idpTrustStore == null) && environment == null) {
            throw new IllegalArgumentException(
                    "Missing property 'environment' in the 'metadata' section of the config: set environment to either PRODUCTION or INTEGRATION to use the default 'hub' and 'idp' metadata truststores, " +
                            "or override both by providing configuration for 'hubTrustStore' and 'idpTrustStore'");
        }


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
