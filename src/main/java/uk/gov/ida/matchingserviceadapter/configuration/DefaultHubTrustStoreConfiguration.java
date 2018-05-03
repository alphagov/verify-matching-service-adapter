package uk.gov.ida.matchingserviceadapter.configuration;

import io.dropwizard.servlets.assets.ResourceNotFoundException;
import uk.gov.ida.matchingserviceadapter.exceptions.EnvironmentNotSupportedException;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_HUB_TRUSTSTORE_NAME;

public class DefaultHubTrustStoreConfiguration extends TrustStoreConfiguration {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */
    private String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";
    private final MatchingServiceAdapterEnvironment environment;

    public DefaultHubTrustStoreConfiguration(MatchingServiceAdapterEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public KeyStore getTrustStore() {
        String trustStoreName;
        switch (environment) {
            case PRODUCTION:
                trustStoreName = PRODUCTION_HUB_TRUSTSTORE_NAME;
                break;
            case INTEGRATION:
                trustStoreName = INTEGRATION_HUB_TRUSTSTORE_NAME;
                break;
            default:
                throw new EnvironmentNotSupportedException("No trust store configured for Matching Service Adapter Environment: " + environment.name());
        }

        InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStoreName);
        if (trustStoreStream == null) {
            throw new ResourceNotFoundException(new FileNotFoundException("Could not load resource from path " + trustStoreName));
        }
        return new KeyStoreLoader().load(trustStoreStream, DEFAULT_TRUST_STORE_PASSWORD);
    }
}
