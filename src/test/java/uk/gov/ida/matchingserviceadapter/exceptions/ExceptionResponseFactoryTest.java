package uk.gov.ida.matchingserviceadapter.exceptions;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_ENTITY_ID;

@RunWith(OpenSAMLRunner.class)
public class ExceptionResponseFactoryTest {

    @Test
    public void createResponse_shouldReturnValidSamlResponse() throws Exception {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(TestEntityIds.STUB_IDP_ONE)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Arrays.asList(encryptionKeyPair));
        ExceptionResponseFactory exceptionResponseFactory = new ExceptionResponseFactory(new OpenSamlXmlObjectFactory(), new IdaKeyStoreCredentialRetriever(keyStore), new IdGenerator());

        String errorMessage = "some message";
        String requestId = UUID.randomUUID().toString();
        Element element = exceptionResponseFactory.createResponse(requestId, TEST_ENTITY_ID, errorMessage);

        Response attributeQueryResponse = (Response) XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);

        assertThat(attributeQueryResponse.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
        assertThat(attributeQueryResponse.getStatus().getStatusMessage().getMessage()).isEqualTo(errorMessage);
        assertThat(attributeQueryResponse.getInResponseTo()).isEqualTo(requestId);
        assertThat(attributeQueryResponse.getIssuer().getValue()).isEqualTo(TEST_ENTITY_ID);
    }
}
