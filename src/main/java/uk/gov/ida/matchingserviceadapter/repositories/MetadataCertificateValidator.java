package uk.gov.ida.matchingserviceadapter.repositories;

import com.google.inject.Inject;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.matchingserviceadapter.exceptions.InvalidSamlMetadataException;

import javax.inject.Named;

public class MetadataCertificateValidator {

    private final MetadataResolver metadataResolver;
    private final MetadataCertificatesRepository metadataCertificatesRepository;
    private final String hubEntityId;
    private final String hubFederationId;

    @Inject
    public MetadataCertificateValidator(
            MetadataResolver metadataResolver,
            MetadataCertificatesRepository metadataCertificatesRepository,
            @Named("HubEntityId") String hubEntityId,
            @Named("HubFederationId") String hubFederationId) {
        this.metadataResolver = metadataResolver;
        this.metadataCertificatesRepository = metadataCertificatesRepository;
        this.hubEntityId = hubEntityId;
        this.hubFederationId = hubFederationId;
    }

    public void validateAll() {
        try {
            Iterable<EntityDescriptor> entityDescriptors = metadataResolver.resolve(new CriteriaSet(new EntityIdCriterion(hubFederationId)));
            entityDescriptors.forEach(entityDescriptor -> {
                String entityID = entityDescriptor.getEntityID();
                if (hubEntityId.equals(entityDescriptor.getEntityID())) {
                    metadataCertificatesRepository.getHubEncryptionCertificates(entityID);
                    metadataCertificatesRepository.getHubSigningCertificates(entityID);
                } else {
                    metadataCertificatesRepository.getIdpSigningCertificates(entityID);
                }
            });
        } catch (ResolverException e) {
            throw new InvalidSamlMetadataException("Metadata could not be read from the metadata service", e);
        }
    }

}
