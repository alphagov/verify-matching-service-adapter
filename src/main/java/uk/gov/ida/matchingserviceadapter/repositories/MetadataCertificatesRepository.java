package uk.gov.ida.matchingserviceadapter.repositories;

import com.google.inject.Inject;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.matchingserviceadapter.exceptions.InvalidSamlMetadataException;

import java.util.List;
import java.util.function.Function;

/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated()
public class MetadataCertificatesRepository {
    private final MetadataResolver metadataResolver;
    private final CertificateValidator certificateValidator;
    private final CertificateExtractor certificateExtractor;

    @Inject
    public MetadataCertificatesRepository(
            MetadataResolver metadataResolver,
            CertificateValidator certificateValidator,
            CertificateExtractor certificateExtractor) {
        this.metadataResolver = metadataResolver;
        this.certificateValidator = certificateValidator;
        this.certificateExtractor = certificateExtractor;
    }

    public List<Certificate> getIdpSigningCertificates(String entityId) {
        return getAndValidateCertificates(entityId, certificateExtractor::extractIdpSigningCertificates);
    }

    public List<Certificate> getHubSigningCertificates(String hubId) {
        return getAndValidateCertificates(hubId, certificateExtractor::extractHubSigningCertificates);
    }

    public List<Certificate> getHubEncryptionCertificates(String hubId) {
        return getAndValidateCertificates(hubId, certificateExtractor::extractHubEncryptionCertificates);
    }

    private List<Certificate> getAndValidateCertificates(String entityId, Function<EntityDescriptor, List<Certificate>> extractor) {
        EntityDescriptor entityDescriptor = getEntityDescriptor(entityId);
        List<Certificate> certificates = extractor.apply(entityDescriptor);
        return certificates;
    }

    private EntityDescriptor getEntityDescriptor(String entityId) {
        try {
            return metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId)));
        } catch (ResolverException e) {
            throw new InvalidSamlMetadataException("Metadata could not be read from the metadata service", e);
        }
    }
}
