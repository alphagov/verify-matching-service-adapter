package uk.gov.ida.matchingserviceadapter.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.saml.core.test.builders.CertificateBuilder;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetadataPublicKeyStoreTest {
    public static String HUB_ID = "hubId";

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    MetadataCertificatesRepository metadataRepository;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    PublicKeyFactory publicKeyFactory;

    private MetadataPublicKeyStore keyStore;

    @Before
    public void setup() {
        keyStore = new MetadataPublicKeyStore(metadataRepository, publicKeyFactory, HUB_ID);
    }

    @Test
    public void getVerifyingKeysForEntity_shouldGetIdpSigningCertificateForEntity() throws Exception {
        String entityId = "entity";
        Certificate idpSigningCertificateOne = CertificateBuilder.aCertificate().withIssuerId(entityId).build();


        Mockito.when(metadataRepository.getIdpSigningCertificates(entityId)).thenReturn(Collections.singletonList(idpSigningCertificateOne));

        PublicKey publicKey = Mockito.mock(PublicKey.class);
        Mockito.when(publicKeyFactory.createPublicKey(idpSigningCertificateOne.getCertificate())).thenReturn(publicKey);

        List<PublicKey> publicKeysForEntity = keyStore.getVerifyingKeysForEntity(entityId);

        assertThat(publicKeysForEntity.size()).isEqualTo(1);
        assertThat(publicKeysForEntity).contains(publicKey);
    }

    @Test
    public void getVerifyingKeysForEntity_shouldGetSigningCertificateForHub() throws Exception {
        Certificate hubSigningCertificate = CertificateBuilder.aCertificate().withIssuerId(HUB_ID).build();
        List<Certificate> hubCertificates= Collections.singletonList(hubSigningCertificate);

        Mockito.when(metadataRepository.getHubSigningCertificates(HUB_ID)).thenReturn(hubCertificates);

        PublicKey publicKey = Mockito.mock(PublicKey.class);
        Mockito.when(publicKeyFactory.createPublicKey(hubSigningCertificate.getCertificate())).thenReturn(publicKey);

        List<PublicKey> publicKeysForEntity = keyStore.getVerifyingKeysForEntity(HUB_ID);

        assertThat(publicKeysForEntity.size()).isEqualTo(1);
        assertThat(publicKeysForEntity).contains(publicKey);
    }

    @Test
    public void getEncryptionKeyForEntity_shouldGetEncryptionCertificateForEntity() throws Exception {
        Certificate encryptionCertificate = CertificateBuilder.aCertificate().withIssuerId(HUB_ID).build();

        Mockito.when(metadataRepository.getHubEncryptionCertificates(HUB_ID)).thenReturn(Collections.singletonList(encryptionCertificate));
        PublicKey publicKey = Mockito.mock(PublicKey.class);
        Mockito.when(publicKeyFactory.createPublicKey(encryptionCertificate.getCertificate())).thenReturn(publicKey);


        PublicKey publicKeyForEntity = keyStore.getEncryptionKeyForEntity(HUB_ID);


        assertThat(publicKeyForEntity).isEqualTo(publicKey);
    }
}
