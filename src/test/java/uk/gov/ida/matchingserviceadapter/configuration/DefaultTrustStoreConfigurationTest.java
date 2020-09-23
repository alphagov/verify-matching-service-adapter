package uk.gov.ida.matchingserviceadapter.configuration;

import org.junit.Test;

import java.security.KeyStoreException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTrustStoreConfigurationTest {
    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationHub() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION.getTrustStoreName(TrustStoreType.HUB)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("test-hub-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationIdp() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION.getTrustStoreName(TrustStoreType.IDP)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("test-idp-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForIntegrationMetadata() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.INTEGRATION.getTrustStoreName(TrustStoreType.METADATA)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("test-root-ca");
        assertThat(aliases).contains("metadata-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionHub() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION.getTrustStoreName(TrustStoreType.HUB)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("hub-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionIdp() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION.getTrustStoreName(TrustStoreType.IDP)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("idp-ca");
    }

    @Test
    public void getTrustStoreShouldReturnCorrectTrustStoreForProductionMetadata() throws KeyStoreException {
        DefaultTrustStoreConfiguration defaultIntegrationHubTrustStore = new DefaultTrustStoreConfiguration(
                MatchingServiceAdapterEnvironment.PRODUCTION.getTrustStoreName(TrustStoreType.METADATA)
        );
        List<String> aliases = Collections.list(defaultIntegrationHubTrustStore.getTrustStore().aliases());

        assertThat(aliases).contains("root-ca");
        assertThat(aliases).contains("metadata-ca");
    }
}
