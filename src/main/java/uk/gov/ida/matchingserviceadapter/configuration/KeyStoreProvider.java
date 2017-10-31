package uk.gov.ida.matchingserviceadapter.configuration;

import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.security.KeyStore;

public class KeyStoreProvider implements Provider<KeyStore> {
    private final TrustStoreConfiguration trustStoreConfiguration;

    @Inject
    public KeyStoreProvider(TrustStoreConfiguration trustStoreConfiguration) {
        this.trustStoreConfiguration = trustStoreConfiguration;
    }

    @Override
    public KeyStore get() {
        return trustStoreConfiguration.getTrustStore();
    }
}
