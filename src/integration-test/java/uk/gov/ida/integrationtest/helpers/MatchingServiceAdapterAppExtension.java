package uk.gov.ida.integrationtest.helpers;

import certificates.values.CACertificates;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.LoggerFactory;
import uk.gov.ida.Constants;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyInfoBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509CertificateBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509DataBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;
import static java.util.Arrays.asList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;

public class MatchingServiceAdapterAppExtension extends DropwizardAppExtension<MatchingServiceAdapterConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/verify-metadata";
    public static final String METADATA_PATH = "/uk/gov/ida/saml/metadata/federation";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    public static final HttpStubRule misconfiguredHubMetadataServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource hubTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("hubCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    public MatchingServiceAdapterAppExtension(boolean badMetadata) {
        this(false, ConfigOverride.config("metadata.uri", "http://localhost:" + misconfiguredHubMetadataServer.getPort() + MatchingServiceAdapterAppExtension.METADATA_PATH));
    }

    public MatchingServiceAdapterAppExtension(Class<MatchingServiceAdapterApplication> applicationClass, String resourceFilePath, ConfigOverride[] defaultConfigOverrides) {
        super(applicationClass, resourceFilePath, defaultConfigOverrides);
    }

    public MatchingServiceAdapterAppExtension(ConfigOverride... otherConfigOverrides) {
        this(false, otherConfigOverrides);
    }

    public MatchingServiceAdapterAppExtension(boolean isCountryEnabled, ConfigOverride... otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                ResourceHelpers.resourceFilePath("verify-matching-service-adapter.yml"),
                MatchingServiceAdapterAppExtension.withDefaultOverrides(
                        isCountryEnabled,
                        true,
                        otherConfigOverrides)
        );
    }

    public MatchingServiceAdapterAppExtension(
            boolean isCountryEnabled,
            String configFile,
            boolean overrideTruststores,
            ConfigOverride... otherConfigOverrides) {
        super(MatchingServiceAdapterApplication.class,
                ResourceHelpers.resourceFilePath(configFile),
                MatchingServiceAdapterAppExtension.withDefaultOverrides(
                        isCountryEnabled,
                        overrideTruststores,
                        otherConfigOverrides
                )
        );
    }

    @Override
    public void before() throws Exception {

        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();

        try {
            InitializationService.initialize();

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

            misconfiguredHubMetadataServer.reset();
            String metadata = new MetadataFactory().metadata(Collections.singletonList(badHubEntityDescriptor()));
            misconfiguredHubMetadataServer.register(METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, metadata);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();

    }


    @Override
    public void after() {
        verifyMetadataServer.reset();
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
                ConfigOverride.config("returnStackTraceInErrorResponse", "true"),
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

    private static EntityDescriptor badHubEntityDescriptor() {
        X509Certificate x509CertificateOne = X509CertificateBuilder.aX509Certificate().withCert(TestCertificateStrings.UNCHAINED_PUBLIC_CERT).build();
        X509Data x509DataOne = X509DataBuilder.aX509Data().withX509Certificate(x509CertificateOne).build();
        KeyInfo signingOne = KeyInfoBuilder.aKeyInfo().withKeyName("signing_one").withX509Data(x509DataOne).build();
        KeyDescriptor keyDescriptorOne = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signingOne).build();
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addKeyDescriptor(keyDescriptorOne)
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(HUB_ENTITY_ID)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .setAddDefaultSpServiceDescriptor(false)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(DateTime.now().plusHours(1))
                    .withSignature(null)
                    .withoutSigning()
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw propagate(e);
        }
    }



}
