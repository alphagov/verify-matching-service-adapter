package uk.gov.ida.matchingserviceadapter.repositories;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Optional;

/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated
public interface MetadataRepository {
    Optional<EntityDescriptor> findByEntityId(String entityId);

    default boolean hasMetadataForEntity(String entityId) {
        return findByEntityId(entityId).isPresent();
    }
}
