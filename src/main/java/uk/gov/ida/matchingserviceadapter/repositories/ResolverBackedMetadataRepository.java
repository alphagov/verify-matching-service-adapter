package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Optional;
/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated
public class ResolverBackedMetadataRepository implements MetadataRepository {
    private final MetadataResolver metadataResolver;
    public ResolverBackedMetadataRepository(final MetadataResolver metadataResolver) {
        this.metadataResolver = metadataResolver;
    }

    @Override
    public Optional<EntityDescriptor> findByEntityId(String entityId) {
        try {
            return Optional.ofNullable(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId))));
        } catch (ResolverException e) {
            return Optional.empty();
        }
    }
}
