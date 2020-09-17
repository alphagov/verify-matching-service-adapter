package uk.gov.ida.matchingserviceadapter.configuration;

import io.dropwizard.servlets.assets.ResourceNotFoundException;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

public class DefaultTrustStoreConfiguration extends TrustStoreConfiguration {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */

    String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";

    private final String trustStoreName;

    public DefaultTrustStoreConfiguration(MatchingServiceAdapterEnvironment environment, TrustStoreType trustStoreType) {
        this.trustStoreName = environment.getTrustStoreName(trustStoreType);
    }

    @Override
    public KeyStore getTrustStore() {
        InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStoreName);
        if (trustStoreStream == null) {
            throw new ResourceNotFoundException(new FileNotFoundException("Could not load resource from path " + trustStoreName));
        }
        return new KeyStoreLoader().load(trustStoreStream, DEFAULT_TRUST_STORE_PASSWORD);
    }
}
