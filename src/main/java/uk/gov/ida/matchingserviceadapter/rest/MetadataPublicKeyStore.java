package uk.gov.ida.matchingserviceadapter.rest;

import com.google.inject.Inject;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.SigningKeyStore;

import javax.inject.Named;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class MetadataPublicKeyStore implements EncryptionKeyStore, SigningKeyStore {

    private final MetadataCertificatesRepository metadataRepository;
    private final PublicKeyFactory publicKeyFactory;
    private final String hubEntityId;

    @Inject
    public MetadataPublicKeyStore(
            MetadataCertificatesRepository metadataRepository,
            PublicKeyFactory publicKeyFactory,
            @Named("HubEntityId") String hubEntityId) {
        this.metadataRepository = metadataRepository;
        this.publicKeyFactory = publicKeyFactory;
        this.hubEntityId = hubEntityId;
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity(String entityId) {
        List<Certificate> certificates;
        if (hubEntityId.equals(entityId)) {
            certificates = metadataRepository.getHubSigningCertificates(entityId);
        } else {
            certificates = metadataRepository.getIdpSigningCertificates(entityId);
        }

        return getPublicKeys(certificates);
    }

    @Override
    public PublicKey getEncryptionKeyForEntity(String entityId) {
        List<Certificate> certificates;

        if (hubEntityId.equals(entityId)) {
            certificates = metadataRepository.getHubEncryptionCertificates(entityId);
        } else {
            certificates = Collections.emptyList();
        }

        return getPublicKeys(certificates).get(0);
    }

    private List<PublicKey> getPublicKeys(List<Certificate> certificates) {
        List<PublicKey> publicKeys = certificates.stream()
                .map(Certificate::getCertificate)
                .map(publicKeyFactory::createPublicKey)
                .collect(Collectors.toList());

        if (publicKeys.isEmpty()) {
            throw new IllegalArgumentException(format("No certificate(s) could be located for entity."));
        }

        return publicKeys;
    }
}
