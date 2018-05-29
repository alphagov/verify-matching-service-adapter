package uk.gov.ida.matchingserviceadapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.util.Duration;
import uk.gov.ida.configuration.ServiceNameConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.HubConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.KeyPairConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.LocalMatchingServiceConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.MatchingServiceAdapterMetadataConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.ServiceInfo;
import uk.gov.ida.matchingserviceadapter.configuration.SigningKeysConfiguration;
import uk.gov.ida.saml.metadata.MetadataConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
public class MatchingServiceAdapterConfiguration extends Configuration implements AssertionLifetimeConfiguration, ServiceNameConfiguration {
    @NotNull
    @Valid
    @JsonProperty
    private Boolean returnStackTraceInResponse = false;

    @NotNull
    @Valid
    @JsonProperty
    private ServiceInfo matchingServiceAdapter;

    @NotNull
    @Valid
    @JsonProperty
    private LocalMatchingServiceConfiguration localMatchingService;

    @NotNull
    @Valid
    @JsonProperty
    private HubConfiguration hub;

    @NotNull
    @Valid
    @JsonProperty
    private MatchingServiceAdapterMetadataConfiguration metadata;

    @NotNull
    @Valid
    @JsonProperty
    private SigningKeysConfiguration signingKeys;

    @NotNull
    @Valid
    @Size(min = 1, max = 2)
    @JsonProperty
    private List<KeyPairConfiguration> encryptionKeys;

    @Valid
    @JsonProperty
    private long clockSkewInSeconds = 30;

    @Valid
    @JsonProperty
    private EuropeanIdentityConfiguration europeanIdentity;

    @NotNull
    @Valid
    @JsonProperty
    private boolean shouldSignWithSHA1 = false;

    protected MatchingServiceAdapterConfiguration() {
    }

    public URI getHubSSOUri() {
        return hub.getSsoUrl();
    }

    @Override
    public Duration getAssertionLifetime() {
        return matchingServiceAdapter.getAssertionLifetime();
    }

    public String getEntityId() {
        return matchingServiceAdapter.getEntityId();
    }

    public long getClockSkew() {
        return clockSkewInSeconds;
    }

    public URI getLocalMatchingServiceMatchUrl() {
        return localMatchingService.getMatchUrl();
    }

    public URI getLocalMatchingServiceAccountCreationUrl() {
        return localMatchingService.getAccountCreationUrl();
    }

    public URI getMatchingServiceAdapterExternalUrl() {
        return matchingServiceAdapter.getExternalUrl();
    }

    public boolean getReturnStackTraceInResponse() {
        return returnStackTraceInResponse;
    }

    public ServiceInfo getServiceInfo() {
        return matchingServiceAdapter;
    }

    @Override
    public String getServiceName() {
        return matchingServiceAdapter.getName();
    }

    public JerseyClientConfiguration getMatchingServiceClientConfiguration() {
        return localMatchingService.getClient();
    }

    public MetadataConfiguration getMetadataConfiguration() {
        return metadata;
    }

    public List<KeyPairConfiguration> getSigningKeys() {
        return signingKeys.getKeyPairs();
    }

    public List<KeyPairConfiguration> getEncryptionKeys() {
        return encryptionKeys;
    }

    public boolean shouldRepublishHubCertificates() {
        return hub.getRepublishHubCertificatesInLocalMetadata();
    }

    public String getHubEntityId() {
        return hub.getHubEntityId();
    }

    public EuropeanIdentityConfiguration getEuropeanIdentity() {
        return europeanIdentity;
    }

    public boolean shouldSignWithSHA1() {
        return shouldSignWithSHA1;
    }

    public static JerseyClientConfiguration getDefaultJerseyClientConfiguration(boolean verifyHostname, boolean trustSelfSignedCertificates) {
        JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTimeout(Duration.seconds(60));
        jerseyClientConfiguration.setTimeToLive(Duration.minutes(10));
        jerseyClientConfiguration.setCookiesEnabled(false);
        jerseyClientConfiguration.setConnectionTimeout(Duration.seconds(4));
        jerseyClientConfiguration.setRetries(3);
        jerseyClientConfiguration.setKeepAlive(Duration.seconds(60));
        jerseyClientConfiguration.setChunkedEncodingEnabled(false);
        jerseyClientConfiguration.setValidateAfterInactivityPeriod(Duration.seconds(5));
        TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setProtocol("TLSv1.2");
        tlsConfiguration.setVerifyHostname(verifyHostname);
        tlsConfiguration.setTrustSelfSignedCertificates(trustSelfSignedCertificates);
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        jerseyClientConfiguration.setGzipEnabledForRequests(false);
        return jerseyClientConfiguration;
    }

    public boolean isEidasEnabled() {
        return getEuropeanIdentity() != null && getEuropeanIdentity().isEnabled();
    }
}
