package uk.gov.ida.matchingserviceadapter.repositories;

import com.google.inject.Inject;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.AttributeService;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.EntitiesDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.exceptions.CouldNotSignMetadataException;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.metadata.transformers.KeyDescriptorsUnmarshaller;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.SignatureFactory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MatchingServiceAdapterMetadataRepository {

    private final KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;
    private final Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer;
    private final CertificateStore certificateStore;
    private final MetadataResolver metadataResolver;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final String hubEntityId;
    private final SignatureFactory signatureFactory;

    @Inject
    public MatchingServiceAdapterMetadataRepository(
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller,
            Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer,
            CertificateStore certificateStore,
            MetadataResolver metadataResolver,
            @Named("HubEntityId") String hubEntityId,
            IdaKeyStore idaKeyStore) {

        this.keyDescriptorsUnmarshaller = keyDescriptorsUnmarshaller;
        this.entitiesDescriptorElementTransformer = entitiesDescriptorElementTransformer;
        this.certificateStore = certificateStore;
        this.metadataResolver = metadataResolver;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.hubEntityId = hubEntityId;

        final MsaTransformersFactory msaTransformersFactory = new MsaTransformersFactory();
        final SignatureAlgorithm signatureAlgorithm = msaTransformersFactory.getSignatureAlgorithm(matchingServiceAdapterConfiguration);
        this.signatureFactory = msaTransformersFactory.getSignatureFactory(true, idaKeyStore, signatureAlgorithm);
    }

    private EntityDescriptor createEntityDescriptor(String entityId) {
        XMLObjectBuilderFactory openSamlBuilderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        EntityDescriptor entityDescriptor = (EntityDescriptor) openSamlBuilderFactory.getBuilder(EntityDescriptor.TYPE_NAME).buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME, EntityDescriptor.TYPE_NAME);
        entityDescriptor.setEntityID(entityId);
        return entityDescriptor;
    }

    public Document getMatchingServiceAdapterMetadata() throws ResolverException, FederationMetadataLoadingException {
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorBuilder().buildObject();
        entitiesDescriptor.setValidUntil(DateTime.now().plusHours(1));

        final EntityDescriptor msaEntityDescriptor = createEntityDescriptor(matchingServiceAdapterConfiguration.getEntityId());
        final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        msaEntityDescriptor.getRoleDescriptors().add(getAttributeAuthorityDescriptor(openSamlXmlObjectFactory));
        msaEntityDescriptor.getRoleDescriptors().add(getIdpSsoDescriptor(openSamlXmlObjectFactory));

        entitiesDescriptor.getEntityDescriptors().add(msaEntityDescriptor);

        if (matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()) {
            entitiesDescriptor.getEntityDescriptors().add(getHubEntityDescriptor());
        }

        if(matchingServiceAdapterConfiguration.isSignMetadataEnabled()) {
            sign(entitiesDescriptor);
        }

        return entitiesDescriptorElementTransformer.apply(entitiesDescriptor).getOwnerDocument();
    }

    private RoleDescriptor getIdpSsoDescriptor(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        IDPSSODescriptor idpssoDescriptor = openSamlXmlObjectFactory.createIDPSSODescriptor();
        idpssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        idpssoDescriptor.getSingleSignOnServices().add(getSsoService());
        idpssoDescriptor.getKeyDescriptors().addAll(getKeyDescriptors());

        return idpssoDescriptor;
    }

    private Collection<? extends KeyDescriptor> getKeyDescriptors() {
        Collection<Certificate> certificates = new ArrayList<>();
        certificates.addAll(certificateStore.getSigningCertificates());
        return keyDescriptorsUnmarshaller.fromCertificates(certificates);
    }

    private SingleSignOnService getSsoService() {
        SingleSignOnService singleSignOnService = new SingleSignOnServiceBuilder().buildObject();

        singleSignOnService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        singleSignOnService.setLocation(matchingServiceAdapterConfiguration.getHubSSOUri().toString());

        return singleSignOnService;
    }

    private RoleDescriptor getAttributeAuthorityDescriptor(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        final AttributeAuthorityDescriptor attributeAuthorityDescriptor = openSamlXmlObjectFactory.createAttributeAuthorityDescriptor();
        attributeAuthorityDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        final AttributeService attributeService = openSamlXmlObjectFactory.createAttributeService();
        attributeService.setLocation(matchingServiceAdapterConfiguration.getMatchingServiceAdapterExternalUrl().toASCIIString());
        attributeService.setBinding(SAMLConstants.SAML2_SOAP11_BINDING_URI);
        attributeAuthorityDescriptor.getAttributeServices().add(attributeService);

        Collection<Certificate> certificates = new ArrayList<>();
        certificates.addAll(certificateStore.getSigningCertificates());
        certificates.addAll(certificateStore.getEncryptionCertificates());
        final List<KeyDescriptor> keyDescriptors = keyDescriptorsUnmarshaller.fromCertificates(certificates);
        attributeAuthorityDescriptor.getKeyDescriptors().addAll(keyDescriptors);

        return attributeAuthorityDescriptor;
    }

    private EntityDescriptor getHubEntityDescriptor() throws ResolverException, FederationMetadataLoadingException {
        Optional<EntityDescriptor> hubDescriptorFromMetadata = Optional.ofNullable(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(hubEntityId))));
        EntityDescriptor hubEntityDescriptor = hubDescriptorFromMetadata.orElseThrow(FederationMetadataLoadingException::new);
        hubEntityDescriptor.detach();
        return hubEntityDescriptor;
    }

    private void sign(EntitiesDescriptor entitiesDescriptor) {
        entitiesDescriptor.setSignature(signatureFactory.createSignature());
        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(entitiesDescriptor).marshall(entitiesDescriptor);
        } catch (MarshallingException e) {
            throw new CouldNotSignMetadataException(e);
        }
        try {
            Signer.signObject(entitiesDescriptor.getSignature());
        } catch (SignatureException e) {
            throw new CouldNotSignMetadataException(e);
        }
    }
}
