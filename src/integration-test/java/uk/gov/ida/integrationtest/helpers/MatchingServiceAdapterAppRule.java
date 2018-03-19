package uk.gov.ida.integrationtest.helpers;

import certificates.values.CACertificates;
import com.nimbusds.jose.JOSEException;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.Constants;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.trustanchor.Generator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_SECONDARY_ENTITY_ID;

public class MatchingServiceAdapterAppRule extends DropwizardAppRule<MatchingServiceAdapterConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/verify-metadata";
    private static final String TRUST_ANCHOR_PATH = "/trust-anchor";
    private static final String METADATA_AGGREGATOR_PATH = "/metadata-aggregator";
    private static final String COUNTRY_METADATA_PATH = "/test-country";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final HttpStubRule metadataAggregatorServer = new HttpStubRule();
    private static final HttpStubRule trustAnchorServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource clientTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("interCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).withCertificate("idpCA", CACertificates.TEST_IDP_CA).build();

    public MatchingServiceAdapterAppRule(ConfigOverride... otherConfigOverrides) {
        this(false, otherConfigOverrides);
    }

    public MatchingServiceAdapterAppRule(boolean isCountryEnabled, ConfigOverride... otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                ResourceHelpers.resourceFilePath("verify-matching-service-adapter.yml"),
                MatchingServiceAdapterAppRule.withDefaultOverrides(isCountryEnabled, otherConfigOverrides)
        );
    }

    @Override
    protected void before() {
        metadataTrustStore.create();
        countryMetadataTrustStore.create();
        clientTrustStore.create();

        JerseyGuiceUtils.reset();
        try {
            InitializationService.initialize();

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

            trustAnchorServer.reset();
            trustAnchorServer.register(TRUST_ANCHOR_PATH, 200, MediaType.APPLICATION_OCTET_STREAM, buildTrustAnchorString());

            metadataAggregatorServer.reset();
            metadataAggregatorServer.register(METADATA_AGGREGATOR_PATH + COUNTRY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    @Override
    protected void after() {
        metadataTrustStore.delete();
        countryMetadataTrustStore.delete();
        clientTrustStore.delete();

        super.after();
    }

    public static ConfigOverride[] withDefaultOverrides(boolean isCountryPresent, ConfigOverride... otherConfigOverrides) {
        List<ConfigOverride> overrides = Stream.of(
            ConfigOverride.config("returnStackTraceInResponse", "true"),
            ConfigOverride.config("server.applicationConnectors[0].port", "0"),
            ConfigOverride.config("server.adminConnectors[0].port", "0"),
            ConfigOverride.config("encryptionKeys[0].privateKey.key", TEST_RP_MS_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("encryptionKeys[0].publicKey.cert", getCertificate(getPrimaryPublicEncryptionCert(HUB_ENTITY_ID))),
            ConfigOverride.config("signingKeys.primary.privateKey.key", TEST_RP_MS_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("signingKeys.primary.publicKey.cert", getCertificate(TEST_RP_MS_PUBLIC_SIGNING_CERT)),
            ConfigOverride.config("signingKeys.secondary.privateKey.key", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("signingKeys.secondary.publicKey.cert", getCertificate(TEST_RP_PUBLIC_SIGNING_CERT)),
            ConfigOverride.config("metadata.trustStore.type", "file"),
            ConfigOverride.config("metadata.trustStore.store", metadataTrustStore.getAbsolutePath()),
            ConfigOverride.config("metadata.trustStore.trustStorePassword", metadataTrustStore.getPassword()),
            ConfigOverride.config("metadata.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("metadata.url", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
            ConfigOverride.config("hub.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("hub.trustStore.type", "file"),
            ConfigOverride.config("hub.trustStore.store", clientTrustStore.getAbsolutePath()),
            ConfigOverride.config("hub.trustStore.trustStorePassword", clientTrustStore.getPassword())
        ).collect(Collectors.toList());

        if (isCountryPresent) {
            List<ConfigOverride> countryOverrides = Stream.of(
                ConfigOverride.config("europeanIdentity.hubConnectorEntityId", HUB_SECONDARY_ENTITY_ID),

                ConfigOverride.config("europeanIdentity.enabled", "true"),

                ConfigOverride.config("europeanIdentity.metadata.uri", "http://localhost:" + metadataAggregatorServer.getPort() + METADATA_AGGREGATOR_PATH + COUNTRY_METADATA_PATH),
                ConfigOverride.config("europeanIdentity.metadata.trustStore.store", countryMetadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("europeanIdentity.metadata.trustStore.trustStorePassword", countryMetadataTrustStore.getPassword()),
                ConfigOverride.config("europeanIdentity.metadata.minRefreshDelay", "60000"),
                ConfigOverride.config("europeanIdentity.metadata.maxRefreshDelay", "600000"),
                ConfigOverride.config("europeanIdentity.metadata.expectedEntityId", "http://stub_idp.acme.org/stub-idp-one/SSO/POST"),
                ConfigOverride.config("europeanIdentity.metadata.jerseyClientName", "country-metadata-client"),

                ConfigOverride.config("europeanIdentity.metadata.client.timeout", "2s"),
                ConfigOverride.config("europeanIdentity.metadata.client.timeToLive", "10m"),
                ConfigOverride.config("europeanIdentity.metadata.client.cookiesEnabled", "false"),
                ConfigOverride.config("europeanIdentity.metadata.client.connectionTimeout", "1s"),
                ConfigOverride.config("europeanIdentity.metadata.client.retries", "3"),
                ConfigOverride.config("europeanIdentity.metadata.client.keepAlive", "60s"),
                ConfigOverride.config("europeanIdentity.metadata.client.chunkedEncodingEnabled", "false"),
                ConfigOverride.config("europeanIdentity.metadata.client.validateAfterInactivityPeriod", "5s"),

                ConfigOverride.config("europeanIdentity.metadata.client.tls.protocol", "TLSv1.2"),
                ConfigOverride.config("europeanIdentity.metadata.client.tls.verifyHostname", "false"),


                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustAnchorUri", "http://localhost:" + trustAnchorServer.getPort() + TRUST_ANCHOR_PATH),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.store", countryMetadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.trustStorePassword", countryMetadataTrustStore.getPassword()),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.metadataBaseUri", "http://localhost:" + metadataAggregatorServer.getPort() + METADATA_AGGREGATOR_PATH),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.minRefreshDelay", "60000"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.maxRefreshDelay", "600000"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.jerseyClientName", "trust-anchor-client"),

                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.timeout", "2s"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.timeToLive", "10m"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.cookiesEnabled", "false"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.connectionTimeout", "1s"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.retries", "3"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.keepAlive", "60s"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.chunkedEncodingEnabled", "false"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.validateAfterInactivityPeriod", "5s"),

                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.tls.protocol", "TLSv1.2"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.tls.verifyHostname", "false"),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.client.tls.trustSelfSignedCertificates", "true")


            ).collect(Collectors.toList());
            overrides.addAll(countryOverrides);
        }

        overrides.addAll(Arrays.asList(otherConfigOverrides));

        return overrides.toArray(new ConfigOverride[overrides.size()]);
    }

    private static String getCertificate(String strippedCertificate) {
        String BEGIN = "-----BEGIN CERTIFICATE-----\n";
        String END = "\n-----END CERTIFICATE-----";
        String certificate = BEGIN + strippedCertificate + END;
        return Base64.encodeBase64String(certificate.getBytes());
    }

    private String buildTrustAnchorString() throws ParseException, JOSEException, CertificateEncodingException {
        PrivateKey trustAnchorKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY));
        X509Certificate trustAnchorCert = new X509CertificateFactory().createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        Generator generator = new Generator(trustAnchorKey, trustAnchorCert);
        HashMap<String, String> trustAnchorMap = new HashMap<>();
        trustAnchorMap.put("test-country", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT);
        return generator.generateFromMap(trustAnchorMap).serialize();
    }
}
