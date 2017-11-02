package uk.gov.ida.integrationtest.helpers;

import certificates.values.CACertificates;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.Constants;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;

public class MatchingServiceAdapterAppRule extends DropwizardAppRule<MatchingServiceAdapterConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/uk/gov/ida/saml/metadata/federation";

    private static final String COUNTRY_METADATA_PATH = "/uk/gov/ida/saml/metadata/country";

    private static final String HUB_CONNECTOR_ENTITY_ID = "http://verify-hub-connector-entity-id";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();

    private static final HttpStubRule countryMetadataServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
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

            countryMetadataServer.reset();
            countryMetadataServer.register(COUNTRY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
        } catch (Exception e) {
            throw propagate(e);
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

    public static ConfigOverride[] withDefaultOverrides(boolean isCountryEnabled, ConfigOverride... otherConfigOverrides) {
        List<ConfigOverride> overrides = Stream.of(
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
            ConfigOverride.config("metadata.trustStore.password", metadataTrustStore.getPassword()),
            ConfigOverride.config("metadata.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("metadata.url", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
            ConfigOverride.config("hub.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("hub.trustStore.type", "file"),
            ConfigOverride.config("hub.trustStore.path", clientTrustStore.getAbsolutePath()),
            ConfigOverride.config("hub.trustStore.password", clientTrustStore.getPassword())

        ).collect(Collectors.toList());

        if (isCountryEnabled) {
            List<ConfigOverride> countryOverrides = Stream.of(
                    ConfigOverride.config("returnStackTraceInResponse", "true"),  // Until eiDAS happy-path through MSA is complete (EID-270)
                    ConfigOverride.config("country.hubConnectorEntityId", HUB_ENTITY_ID),

                    ConfigOverride.config("country.metadata.uri", "http://localhost:" + countryMetadataServer.getPort() + COUNTRY_METADATA_PATH),
                    ConfigOverride.config("country.metadata.trustStore.path", countryMetadataTrustStore.getAbsolutePath()),
                    ConfigOverride.config("country.metadata.trustStore.password", countryMetadataTrustStore.getPassword()),
                    ConfigOverride.config("country.metadata.minRefreshDelay", "60000"),
                    ConfigOverride.config("country.metadata.maxRefreshDelay", "600000"),
                    ConfigOverride.config("country.metadata.expectedEntityId", "http://stub_idp.acme.org/stub-idp-one/SSO/POST"),
                    ConfigOverride.config("country.metadata.jerseyClientName", "country-metadata-client"),

                    ConfigOverride.config("country.metadata.client.timeout", "2s"),
                    ConfigOverride.config("country.metadata.client.timeToLive", "10m"),
                    ConfigOverride.config("country.metadata.client.cookiesEnabled", "false"),
                    ConfigOverride.config("country.metadata.client.connectionTimeout", "1s"),
                    ConfigOverride.config("country.metadata.client.retries", "3"),
                    ConfigOverride.config("country.metadata.client.keepAlive", "60s"),
                    ConfigOverride.config("country.metadata.client.chunkedEncodingEnabled", "false"),
                    ConfigOverride.config("country.metadata.client.validateAfterInactivityPeriod", "5s"),

                    ConfigOverride.config("country.metadata.client.tls.protocol", "TLSv1.2"),
                    ConfigOverride.config("country.metadata.client.tls.verifyHostname", "false"),
                    ConfigOverride.config("country.metadata.client.tls.trustSelfSignedCertificates", "true")

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
        return Base64.getEncoder().encodeToString(certificate.getBytes());
    }
}
