package uk.gov.ida.integrationtest.helpers;

import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyStore;

public class TestMetadataResolverConfigurationBuilder {

    private KeyStore trustStore;
    private URI uri;
    private String expectedEntityId;
    private String hubFederationId;

    private TestMetadataResolverConfigurationBuilder() {}

    public static TestMetadataResolverConfigurationBuilder aConfig() {
        return new TestMetadataResolverConfigurationBuilder();
    }

    public MetadataResolverConfiguration build() {
        return new TestMetadataResolverConfiguration(this.trustStore, this.uri, this.expectedEntityId, this.hubFederationId);
    }

    public TestMetadataResolverConfigurationBuilder withMsaEntityId(String msaEntityId) {
        this.expectedEntityId = msaEntityId;
        return this;
    }

    public TestMetadataResolverConfigurationBuilder withUri(String uri) {
        this.uri = UriBuilder.fromUri(uri).build();
        return this;
    }

    public TestMetadataResolverConfigurationBuilder withTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public TestMetadataResolverConfigurationBuilder withHubFederationId(String hubFederationId) {
        this.hubFederationId = hubFederationId;
        return this;
    }

}
