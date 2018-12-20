package uk.gov.ida.integrationtest;

import certificates.values.CACertificates;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.CertificateEntry;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.integrationtest.helpers.TestMetadataResolverConfigurationBuilder;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;

public class MetadataIntegrationTest {

    private static final String MSA_SIGNING_PRIMARY = "http://matching-service-signing/primary";
    private static final String MSA_SIGNING_SECONDARY = "http://matching-service-signing/secondary";
    private static final String MSA_ENCRYPTION_PRIMARY = "http://matching-service-encryption/primary";

    private static KeyStoreResource rpTrustStore;

    @BeforeClass
    public static void setupResolver() {
        rpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificates(ImmutableList.of(new CertificateEntry("test_root_ca", CACertificates.TEST_ROOT_CA),
                        new CertificateEntry("test_rp_ca", CACertificates.TEST_RP_CA)))
                .build();
        rpTrustStore.create();
    }

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> applicationRule = new MatchingServiceAdapterAppRule(
        ConfigOverride.config("signingKeys.primary.publicKey.name", MSA_SIGNING_PRIMARY),
        ConfigOverride.config("signingKeys.primary.publicKey.type", "x509"),
        ConfigOverride.config("signingKeys.primary.publicKey.cert", TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT.replaceAll("\n", "")),
        ConfigOverride.config("signingKeys.primary.privateKey.type", "encoded"),
        ConfigOverride.config("signingKeys.primary.privateKey.key", TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("signingKeys.secondary.publicKey.name", MSA_SIGNING_SECONDARY),
        ConfigOverride.config("encryptionKeys[0].publicKey.name", MSA_ENCRYPTION_PRIMARY),
        ConfigOverride.config("shouldRepublishHubCertificates", "false"),
        ConfigOverride.config("signMetadataEnabled", "true"),
        ConfigOverride.config("matchingServiceAdapter.entityId", TEST_RP_MS)
    );

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> applicationRuleWithUnexpectedSigningKeyInTheBaggingArea = new MatchingServiceAdapterAppRule(
            ConfigOverride.config("signingKeys.primary.publicKey.name", MSA_SIGNING_PRIMARY),
            ConfigOverride.config("signingKeys.primary.publicKey.type", "x509"),
            ConfigOverride.config("signingKeys.primary.publicKey.cert", TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT.replaceAll("\n", "")),
            ConfigOverride.config("signingKeys.primary.privateKey.type", "encoded"),
            ConfigOverride.config("signingKeys.primary.privateKey.key", TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
            ConfigOverride.config("signingKeys.secondary.publicKey.name", MSA_SIGNING_SECONDARY),
            ConfigOverride.config("encryptionKeys[0].publicKey.name", MSA_ENCRYPTION_PRIMARY),
            ConfigOverride.config("shouldRepublishHubCertificates", "false"),
            ConfigOverride.config("signMetadataEnabled", "true"),
            ConfigOverride.config("matchingServiceAdapter.entityId", TEST_RP_MS)
    );

    @Test
    public void shouldGenerateValidMetadataFromLocalConfiguration() throws Exception {
        MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolverWithClient(TestMetadataResolverConfigurationBuilder.aConfig()
                .withMsaEntityId(TEST_RP_MS)
                .withUri("http://localhost:" + applicationRule.getLocalPort() + "/matching-service/SAML2/metadata")
                .withTrustStore(rpTrustStore.getKeyStore())
                .withHubFederationId("")//"this is not set in the generated metadata
                .build(),
                true,
                JerseyClientBuilder.createClient());

        EntityDescriptor descriptor = metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TEST_RP_MS)));
        AttributeAuthorityDescriptor attributeAuthorityDescriptor = descriptor.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS);
        Map<UsageType, List<KeyDescriptor>> keysByUsage = attributeAuthorityDescriptor.getKeyDescriptors().stream()
                .collect(groupingBy(KeyDescriptor::getUse));

        assertThat(keysByUsage.get(UsageType.SIGNING)).hasSize(2);
        assertThat(keysByUsage.get(UsageType.ENCRYPTION)).hasSize(1);

        assertThat(getCertificateNames(keysByUsage, UsageType.SIGNING)).contains(MSA_SIGNING_PRIMARY, MSA_SIGNING_SECONDARY);
        assertThat(getCertificateNames(keysByUsage, UsageType.ENCRYPTION)).contains(MSA_ENCRYPTION_PRIMARY);

        IDPSSODescriptor idpssoDescriptor = descriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(idpssoDescriptor).isNotNull();
        assertThat(idpssoDescriptor.getSingleSignOnServices()).hasSize(1);

        keysByUsage = idpssoDescriptor.getKeyDescriptors().stream().collect(groupingBy(KeyDescriptor::getUse));
        assertThat(keysByUsage.get(UsageType.SIGNING)).hasSize(2);
        assertThat(getCertificateNames(keysByUsage, UsageType.SIGNING)).contains(MSA_SIGNING_PRIMARY, MSA_SIGNING_SECONDARY);
    }

    @Test
    public void shouldGenerateInvalidMetadataFromLocalConfiguration() throws Exception {
        MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolverWithClient(TestMetadataResolverConfigurationBuilder.aConfig()
                .withMsaEntityId(TEST_RP_MS)
                .withUri("http://localhost:" + applicationRuleWithUnexpectedSigningKeyInTheBaggingArea.getLocalPort() + "/matching-service/SAML2/metadata")
                .withTrustStore(rpTrustStore.getKeyStore())
                .withHubFederationId("")//"this is not set in the generated metadata
                .build(),
                true,
                JerseyClientBuilder.createClient());

        EntityDescriptor descriptor = metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TEST_RP_MS)));
        assertThat(descriptor).isNull();
    }

    private List<String> getCertificateNames(Map<UsageType, List<KeyDescriptor>> keysByUsage, UsageType keyUsage) {
        return keysByUsage.get(keyUsage).stream()
                    .flatMap(kd -> kd.getKeyInfo().getKeyNames().stream())
                    .map(XSString::getValue)
                    .collect(Collectors.toList());
    }

}
