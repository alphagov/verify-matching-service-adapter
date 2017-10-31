package uk.gov.ida.matchingserviceadapter.repositories;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Optional;

public interface MetadataRepository {
    Optional<EntityDescriptor> findByEntityId(String entityId);

    default boolean hasMetadataForEntity(String entityId) {
        return findByEntityId(entityId).isPresent();
    }
}
