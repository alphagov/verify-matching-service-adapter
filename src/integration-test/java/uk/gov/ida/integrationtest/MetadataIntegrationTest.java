package uk.gov.ida.integrationtest;

import io.dropwizard.testing.ConfigOverride;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;

public class MetadataIntegrationTest {

    private static final String MSA_SIGNING_PRIMARY = "http://matching-service-signing/primary";
    private static final String MSA_SIGNING_SECONDARY = "http://matching-service-signing/secondary";
    private static final String MSA_ENCRYPTION_PRIMARY = "http://matching-service-encryption/primary";

    @RegisterExtension
    static MatchingServiceAdapterAppExtension applicationRule = new MatchingServiceAdapterAppExtension(
            ConfigOverride.config("signingKeys.primary.publicKey.name", MSA_SIGNING_PRIMARY),
            ConfigOverride.config("signingKeys.secondary.publicKey.name", MSA_SIGNING_SECONDARY),
            ConfigOverride.config("encryptionKeys[0].publicKey.name", MSA_ENCRYPTION_PRIMARY)
    );


    @Test
    public void shouldGenerateValidMetadataFromLocalConfiguration() throws Exception {
        HTTPMetadataResolver httpMetadataResolver = new HTTPMetadataResolver(new Timer(), HttpClientBuilder.create().build(),
                "http://localhost:" + applicationRule.getLocalPort() + "/matching-service/SAML2/metadata");
        BasicParserPool basicParserPool = new BasicParserPool();
        basicParserPool.initialize();
        httpMetadataResolver.setParserPool(basicParserPool);
        httpMetadataResolver.setId("test id");
        httpMetadataResolver.initialize();

        httpMetadataResolver.refresh();

        EntityDescriptor descriptor = httpMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TEST_RP_MS)));
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

    private List<String> getCertificateNames(Map<UsageType, List<KeyDescriptor>> keysByUsage, UsageType keyUsage) {
        return keysByUsage.get(keyUsage).stream()
                    .flatMap(kd -> kd.getKeyInfo().getKeyNames().stream())
                    .map(XSString::getValue)
                    .collect(Collectors.toList());
    }

}
