package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.metadata.StringBackedMetadataResolver;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntityDescriptorFactory;
import uk.gov.ida.saml.metadata.transformers.KeyDescriptorsUnmarshaller;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.shared.utils.xml.XmlUtils;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.shared.utils.string.StringEncoding.toBase64Encoded;
import static uk.gov.ida.shared.utils.xml.XmlUtils.writeToString;

@RunWith(OpenSAMLMockitoRunner.class)
public class MatchingServiceAdapterMetadataRepositoryTest {

    @Mock
    private MatchingServiceAdapterConfiguration msaConfiguration;

    private KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;

    private Function<EntitiesDescriptor, Element> entityDescriptorElementTransformer;

    private MatchingServiceAdapterMetadataRepository matchingServiceAdapterMetadataRepository;

    @Mock
    private MetadataResolver msaMetadataResolver;

    @Mock
    private CertificateStore certificateStore;
    private String entityId;
    private String hubSsoEndPoint = "http://localhost:50300/SAML2/SSO";

    @Before
    public void setUp() throws Exception {
        entityId = "http://issuer";
        when(msaConfiguration.getEntityId()).thenReturn(entityId);
        when(msaConfiguration.isSignMetadataEnabled()).thenReturn(true);
        when(msaConfiguration.getMatchingServiceAdapterExternalUrl()).thenReturn(URI.create("http://localhost"));
        when(certificateStore.getEncryptionCertificates()).thenReturn(asList());
        when(msaMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID)))).thenReturn(new EntityDescriptorFactory().hubEntityDescriptor());
        when(msaConfiguration.getHubSSOUri()).thenReturn(URI.create(hubSsoEndPoint));
        when(msaConfiguration.shouldRepublishHubCertificates()).thenReturn(false);

        entityDescriptorElementTransformer = new CoreTransformersFactory().getXmlObjectToElementTransformer();
        keyDescriptorsUnmarshaller = new KeyDescriptorsUnmarshaller(new OpenSamlXmlObjectFactory());

        final X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        final PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY));
        final X509Certificate msaSigningCert = x509CertificateFactory.createCertificate(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        final PublicKey publicKey = msaSigningCert.getPublicKey();
        final KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        final IdaKeyStore keyStore = new IdaKeyStore(msaSigningCert, signingKeyPair, Arrays.asList());

        matchingServiceAdapterMetadataRepository = new MatchingServiceAdapterMetadataRepository(
                msaConfiguration,
                keyDescriptorsUnmarshaller,
                entityDescriptorElementTransformer,
                certificateStore,
                msaMetadataResolver,
                TestEntityIds.HUB_ENTITY_ID,
                keyStore);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldNotReturnTheHubEntityDescriptorWhenConfiguredToNotNeedHubCerts() throws Exception {
        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        assertThat(getEntityDescriptor(matchingServiceAdapterMetadata, TestEntityIds.HUB_ENTITY_ID)).isNull();
    }

    @Test
    public void shouldHaveAnIDPSSODescriptor() throws ResolverException, FederationMetadataLoadingException {
        when(certificateStore.getSigningCertificates()).thenReturn(asList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        IDPSSODescriptor idpssoDescriptor = msa.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(idpssoDescriptor).isNotNull();
        assertThat(idpssoDescriptor.getSingleSignOnServices()).hasSize(1);
        assertThat(idpssoDescriptor.getSingleSignOnServices().get(0).getLocation()).isEqualTo(hubSsoEndPoint);

        // Shibboleth SP doesn't like the xsi:type="md:EndpointType" attribute on the SingleSignOnService element:
        assertThat(idpssoDescriptor.getSingleSignOnServices().get(0).getSchemaType()).isNull();

        assertThat(idpssoDescriptor.getKeyDescriptors()).hasSize(1);
    }

    @Test
    public void shouldHaveOneSigningKeyDescriptorWhenMsaIsConfiguredWithNoSecondaryPublicSigningKey() throws Exception {
        when(certificateStore.getSigningCertificates()).thenReturn(asList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        assertThat(msa.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(1);
    }

    @Test
    public void shouldHaveTwoSigningKeyDescriptorsWhenMsaIsConfiguredWithSecondaryPublicSigningKey() throws Exception {
        when(certificateStore.getSigningCertificates()).thenReturn(asList(getCertificate(), getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor msa = getEntityDescriptor(matchingServiceAdapterMetadata, entityId);

        assertThat(msa.getRoleDescriptors().size()).isEqualTo(2);
        assertThat(msa.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnTheHubEntityDescriptorInMSAMetadataWhenConfiguredToDoSo() throws Exception {
        when(msaConfiguration.shouldRepublishHubCertificates()).thenReturn(true);
        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntityDescriptor hub = getEntityDescriptor(matchingServiceAdapterMetadata, TestEntityIds.HUB_ENTITY_ID);

        assertThat(hub.getRoleDescriptors().size()).isEqualTo(1);
        assertThat(hub.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().size()).isEqualTo(3);
    }

    @Test(expected = FederationMetadataLoadingException.class)
    public void shouldThrowAnExceptionIfFederationMetadataCannotBeLoadedORHubIsMissing() throws Exception {
        when(msaConfiguration.shouldRepublishHubCertificates()).thenReturn(true);
        when(msaMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID)))).thenReturn(null);
        when(certificateStore.getSigningCertificates()).thenReturn(asList(getCertificate()));

        matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
    }

    @Test
    public void shouldBeAbleToLoadMSAMetadataUsingMetadataResolver() throws Exception {
        when(msaConfiguration.shouldRepublishHubCertificates()).thenReturn(true);

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        String metadata = XmlUtils.writeToString(matchingServiceAdapterMetadata);

        StringBackedMetadataResolver stringBackedMetadataResolver = new StringBackedMetadataResolver(metadata);
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        stringBackedMetadataResolver.setParserPool(pool);
        stringBackedMetadataResolver.setId("Some ID");
        stringBackedMetadataResolver.initialize();

        assertThat(stringBackedMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId))).getEntityID()).isEqualTo(entityId);
        assertThat(stringBackedMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID))).getEntityID()).isEqualTo(TestEntityIds.HUB_ENTITY_ID);
    }

    @Test
    public void shouldGenerateMetadataValidFor1Hour() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());
        when(certificateStore.getSigningCertificates()).thenReturn(asList(getCertificate()));

        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        assertThat(entitiesDescriptor.getValidUntil()).isEqualTo(DateTime.now(DateTimeZone.UTC).plusHours(1));
    }

    @Test
    public void shouldReturnSignedMetadataWhenConfiguredToDoSo() throws Exception {
        when(msaConfiguration.isSignMetadataEnabled()).thenReturn(true);
        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntitiesDescriptor descriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        assertThat(descriptor.getSignature()).isNotNull();
    }

    @Test
    public void shouldReturnUnsignedMetadataWhenConfiguredToDoSo() throws Exception {
        when(msaConfiguration.isSignMetadataEnabled()).thenReturn(false);
        Document matchingServiceAdapterMetadata = matchingServiceAdapterMetadataRepository.getMatchingServiceAdapterMetadata();
        EntitiesDescriptor descriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        assertThat(descriptor.getSignature()).isNull();
    }


    private Certificate getCertificate() {
        return new Certificate(entityId, TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, Certificate.KeyUse.Signing);
    }

    private EntityDescriptor getEntityDescriptor(Document matchingServiceAdapterMetadata, String entityId) {
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        EntityDescriptor matchingEntityDescriptor = null;
        for (EntityDescriptor entityDescriptor : entitiesDescriptor.getEntityDescriptors()) {
            if (entityDescriptor.getEntityID().equals(entityId)) {
                matchingEntityDescriptor = entityDescriptor;
            }
        }
        return matchingEntityDescriptor;
    }

    private EntitiesDescriptor getEntitiesDescriptor(Document matchingServiceAdapterMetadata) {
        StringToOpenSamlObjectTransformer<XMLObject> stringtoOpenSamlObjectTransformer = new CoreTransformersFactory().getStringtoOpenSamlObjectTransformer(input -> {});

        return (EntitiesDescriptor) stringtoOpenSamlObjectTransformer.apply(toBase64Encoded(writeToString(matchingServiceAdapterMetadata)));
    }
}
