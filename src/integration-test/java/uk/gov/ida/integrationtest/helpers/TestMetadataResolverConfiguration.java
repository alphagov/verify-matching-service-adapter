package uk.gov.ida.integrationtest.helpers;

import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import java.net.URI;
import java.security.KeyStore;

public class TestMetadataResolverConfiguration implements MetadataResolverConfiguration {
    private KeyStore trustStore;
    private URI uri;
    private String expectedEntityId;
    private String hubFederationId;

    TestMetadataResolverConfiguration(KeyStore trustStore, URI uri, String expectedEntityId, String hubFederationId) {
        this.trustStore = trustStore;
        this.uri = uri;
        this.expectedEntityId = expectedEntityId;
        this.hubFederationId = hubFederationId;
    }

    @Override
    public KeyStore getTrustStore() {
        return trustStore;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Long getMinRefreshDelay() {
        return Long.valueOf(1);
    }

    @Override
    public Long getMaxRefreshDelay() {
        return Long.valueOf(60);
    }

    @Override
    public String getExpectedEntityId() {
        return expectedEntityId;
    }

    @Override
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build();
    }

    @Override
    public String getJerseyClientName() {
        return this.getClass().getName();
    }

    @Override
    public String getHubFederationId() {
        return hubFederationId;
    }
}
