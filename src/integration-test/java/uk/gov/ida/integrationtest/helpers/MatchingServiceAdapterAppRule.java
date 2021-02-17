package uk.gov.ida.integrationtest.helpers;

import certificates.values.CACertificates;
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
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;

public class MatchingServiceAdapterAppRule extends DropwizardAppRule<MatchingServiceAdapterConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/verify-metadata";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource hubTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("hubCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    public MatchingServiceAdapterAppRule(ConfigOverride... otherConfigOverrides) {
        this(false, otherConfigOverrides);
    }

    public MatchingServiceAdapterAppRule(boolean isCountryEnabled, ConfigOverride... otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                ResourceHelpers.resourceFilePath("verify-matching-service-adapter.yml"),
                MatchingServiceAdapterAppRule.withDefaultOverrides(
                        isCountryEnabled,
                        true,
                        otherConfigOverrides)
        );
    }

    public MatchingServiceAdapterAppRule(
            boolean isCountryEnabled,
            String configFile,
            boolean overrideTruststores,
            ConfigOverride... otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                ResourceHelpers.resourceFilePath(configFile),
                MatchingServiceAdapterAppRule.withDefaultOverrides(
                        isCountryEnabled,
                        overrideTruststores,
                        otherConfigOverrides
                )
        );
    }

    @Override
    protected void before() {
        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();

        JerseyGuiceUtils.reset();
        try {
            InitializationService.initialize();

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    @Override
    protected void after() {
        metadataTrustStore.delete();
        hubTrustStore.delete();
        idpTrustStore.delete();

        super.after();
    }

    public static ConfigOverride[] withDefaultOverrides(
            boolean isCountryPresent,
            boolean overrideTruststores,
            ConfigOverride... otherConfigOverrides) {
        List<ConfigOverride> overrides = Stream.of(
            ConfigOverride.config("returnStackTraceInResponse", "true"),
            ConfigOverride.config("clockSkewInSeconds", "60"),
            ConfigOverride.config("server.applicationConnectors[0].port", "0"),
            ConfigOverride.config("server.adminConnectors[0].port", "0"),
            ConfigOverride.config("encryptionKeys[0].privateKey.key", TEST_RP_MS_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("encryptionKeys[0].privateKey.type", "encoded"),
            ConfigOverride.config("encryptionKeys[0].publicKey.type", "encoded"),
            ConfigOverride.config("encryptionKeys[0].publicKey.cert", getCertificate(getPrimaryPublicEncryptionCert(HUB_ENTITY_ID))),
            ConfigOverride.config("signingKeys.primary.privateKey.key", TEST_RP_MS_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("signingKeys.primary.privateKey.type", "encoded"),
            ConfigOverride.config("signingKeys.primary.publicKey.type", "encoded"),
            ConfigOverride.config("signingKeys.primary.publicKey.cert", getCertificate(TEST_RP_MS_PUBLIC_SIGNING_CERT)),
            ConfigOverride.config("signingKeys.secondary.privateKey.key", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("signingKeys.secondary.privateKey.type", "encoded"),
            ConfigOverride.config("signingKeys.secondary.publicKey.type", "encoded"),
            ConfigOverride.config("signingKeys.secondary.publicKey.cert", getCertificate(TEST_RP_PUBLIC_SIGNING_CERT)),
            ConfigOverride.config("metadata.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
            ConfigOverride.config("hub.hubEntityId", HUB_ENTITY_ID)
        ).collect(Collectors.toList());

        if (overrideTruststores) {
            List<ConfigOverride> trustStoreOverrides = Stream.of(
                ConfigOverride.config("metadata.trustStore.type", "file"),
                ConfigOverride.config("metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("metadata.trustStore.password", metadataTrustStore.getPassword()),
                ConfigOverride.config("metadata.hubTrustStore.type", "file"),
                ConfigOverride.config("metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
                ConfigOverride.config("metadata.hubTrustStore.password", hubTrustStore.getPassword()),
                ConfigOverride.config("metadata.idpTrustStore.type", "file"),
                ConfigOverride.config("metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
                ConfigOverride.config("metadata.idpTrustStore.password", idpTrustStore.getPassword())
            ).collect(Collectors.toList());
            overrides.addAll(trustStoreOverrides);
        }

        if (isCountryPresent) {
            overrides.add(ConfigOverride.config("europeanIdentity.enabled", "true"));
        }

        overrides.addAll(asList(otherConfigOverrides));

        return overrides.toArray(new ConfigOverride[overrides.size()]);
    }

    private static String getCertificate(String strippedCertificate) {
        String certificate = BEGIN_CERT + strippedCertificate + END_CERT;
        return Base64.encodeBase64String(certificate.getBytes());
    }
}
