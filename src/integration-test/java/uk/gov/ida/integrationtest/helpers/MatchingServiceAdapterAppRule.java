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
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.Constants;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.trustanchor.Generator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_SECONDARY_ENTITY_ID;
import static uk.gov.ida.saml.metadata.ResourceEncoder.entityIdAsResource;

public class MatchingServiceAdapterAppRule extends DropwizardAppRule<MatchingServiceAdapterConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/verify-metadata";
    private static final String TRUST_ANCHOR_PATH = "/trust-anchor";
    private static final String METADATA_AGGREGATOR_PATH = "/metadata-aggregator";
    private static final String COUNTRY_METADATA_PATH = "/test-country";
    private static final String METADATA_SOURCE_PATH = "/metadata-source";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final HttpStubRule metadataAggregatorServer = new HttpStubRule();
    private static final HttpStubRule trustAnchorServer = new HttpStubRule();

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource hubTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("hubCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    private String countryEntityId;

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
        hubTrustStore.create();
        idpTrustStore.create();
        countryMetadataTrustStore.create();

        countryEntityId = "https://localhost:12345" + METADATA_AGGREGATOR_PATH + COUNTRY_METADATA_PATH;

        JerseyGuiceUtils.reset();
        try {
            InitializationService.initialize();
            String testCountryMetadata = new MetadataFactory().singleEntityMetadata(buildTestCountryEntityDescriptor());

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

            trustAnchorServer.reset();
            trustAnchorServer.register(TRUST_ANCHOR_PATH, 200, MediaType.APPLICATION_OCTET_STREAM, buildTrustAnchorString());

            metadataAggregatorServer.reset();
            metadataAggregatorServer.register(METADATA_SOURCE_PATH + "/" + entityIdAsResource(countryEntityId), 200, Constants.APPLICATION_SAMLMETADATA_XML, testCountryMetadata);
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
        countryMetadataTrustStore.delete();

        super.after();
    }

    public static ConfigOverride[] withDefaultOverrides(boolean isCountryPresent, ConfigOverride... otherConfigOverrides) {
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
            ConfigOverride.config("metadata.trustStore.type", "file"),
            ConfigOverride.config("metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
            ConfigOverride.config("metadata.trustStore.password", metadataTrustStore.getPassword()),
            ConfigOverride.config("metadata.hubTrustStore.type", "file"),
            ConfigOverride.config("metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
            ConfigOverride.config("metadata.hubTrustStore.password", hubTrustStore.getPassword()),
            ConfigOverride.config("metadata.idpTrustStore.type", "file"),
            ConfigOverride.config("metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
            ConfigOverride.config("metadata.idpTrustStore.password", idpTrustStore.getPassword()),
            ConfigOverride.config("metadata.hubEntityId", HUB_ENTITY_ID),
            ConfigOverride.config("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
            ConfigOverride.config("hub.hubEntityId", HUB_ENTITY_ID)
        ).collect(Collectors.toList());

        if (isCountryPresent) {
            List<ConfigOverride> countryOverrides = Stream.of(
                ConfigOverride.config("europeanIdentity.hubConnectorEntityId", HUB_SECONDARY_ENTITY_ID),
                ConfigOverride.config("europeanIdentity.enabled", "true"),

                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustAnchorUri", "http://localhost:" + trustAnchorServer.getPort() + TRUST_ANCHOR_PATH),
                    ConfigOverride.config("europeanIdentity.aggregatedMetadata.metadataSourceUri", "http://localhost:" + metadataAggregatorServer.getPort() + METADATA_SOURCE_PATH),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.store", countryMetadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.trustStorePassword", countryMetadataTrustStore.getPassword()),
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

        overrides.addAll(asList(otherConfigOverrides));

        return overrides.toArray(new ConfigOverride[overrides.size()]);
    }

    public String getCountryEntityId() {
        return countryEntityId;
    }

    private static String getCertificate(String strippedCertificate) {
        String certificate = BEGIN_CERT + strippedCertificate + END_CERT;
        return Base64.encodeBase64String(certificate.getBytes());
    }

    private String buildTrustAnchorString() throws JOSEException, CertificateEncodingException {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        PrivateKey trustAnchorKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY));
        X509Certificate trustAnchorCert = x509CertificateFactory.createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        Generator generator = new Generator(trustAnchorKey, trustAnchorCert);
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        X509Certificate metadataCACert = x509CertificateFactory.createCertificate(CACertificates.TEST_METADATA_CA.replace(BEGIN_CERT, "").replace(END_CERT, "").replace("\n", ""));
        trustAnchorMap.put(countryEntityId, singletonList(metadataCACert));
        return generator.generateFromMap(trustAnchorMap).serialize();
    }

    private EntityDescriptor buildTestCountryEntityDescriptor() throws Exception {
        KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                .withX509ForSigning(STUB_COUNTRY_PUBLIC_PRIMARY_CERT)
                .build();

        IDPSSODescriptor idpSsoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                .withoutDefaultSigningKey()
                .addKeyDescriptor(signingKeyDescriptor)
                .build();

        Signature signature = SignatureBuilder.aSignature()
                .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
                .build();

        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(countryEntityId)
                .withIdpSsoDescriptor(idpSsoDescriptor)
                .setAddDefaultSpServiceDescriptor(false)
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withSignature(signature)
                .build();
    }
}
