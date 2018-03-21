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

/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated()
public class MetadataCertificatesRepository {
    private final MetadataResolver metadataResolver;
    private final CertificateExtractor certificateExtractor;

    @Inject
    public MetadataCertificatesRepository(
            MetadataResolver metadataResolver,
            CertificateExtractor certificateExtractor) {
        this.metadataResolver = metadataResolver;
        this.certificateExtractor = certificateExtractor;
    }

    public List<Certificate> getIdpSigningCertificates(String entityId) {
        return certificateExtractor.extractIdpSigningCertificates(getEntityDescriptor(entityId));
    }

    public List<Certificate> getHubSigningCertificates(String hubId) {
        return certificateExtractor.extractHubSigningCertificates(getEntityDescriptor(hubId));
    }

    public List<Certificate> getHubEncryptionCertificates(String hubId) {
        return certificateExtractor.extractHubEncryptionCertificates(getEntityDescriptor(hubId));
    }

    private EntityDescriptor getEntityDescriptor(String entityId) {
        try {
            return metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId)));
        } catch (ResolverException e) {
            throw new InvalidSamlMetadataException("Metadata could not be read from the metadata service", e);
        }
    }
}
