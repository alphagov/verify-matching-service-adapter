package uk.gov.ida.matchingserviceadapter.configuration;

import org.junit.Test;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTrustStoreConfigurationTest {
    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationHub() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION,
                TrustStoreType.HUB
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("test-hub-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationIdp() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION,
                TrustStoreType.IDP
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("test-idp-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationMetadata() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION,
                TrustStoreType.METADATA
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("metadata-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionHub() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION,
                TrustStoreType.HUB
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("hub-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionIdp() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION,
                TrustStoreType.IDP
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("idp-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionMetadata() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION,
                TrustStoreType.METADATA
        );
        ArrayList<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("metadata-ca");
    }
}
