package uk.gov.ida.integrationtest.helpers;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.test.HardCodedKeyStore;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.transformers.outbound.decorators.SamlAttributeQueryAssertionEncrypter;
import uk.gov.ida.saml.core.transformers.outbound.decorators.SamlSignatureSigner;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.deserializers.parser.SamlObjectParser;
import uk.gov.ida.saml.hub.transformers.outbound.AttributeQueryToElementTransformer;
import uk.gov.ida.saml.hub.transformers.outbound.decorators.SamlAttributeQueryAssertionSignatureSigner;
import uk.gov.ida.saml.hub.transformers.outbound.decorators.SigningRequestAbstractTypeSignatureCreator;
import uk.gov.ida.saml.security.EncrypterFactory;
import uk.gov.ida.saml.security.EncryptionCredentialFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.saml.serializers.XmlObjectToElementTransformer;

import javax.ws.rs.client.Entity;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;

public class RequestHelper {

    public static Response makeAttributeQueryRequest(String uri, AttributeQuery attributeQuery, SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm, String hubEntityId) {
        ElementToOpenSamlXMLObjectTransformer<Response> responseElementToOpenSamlXMLObjectTransformer = new ElementToOpenSamlXMLObjectTransformer<>(new SamlObjectParser());

        Document attributeQueryDocument = getAttributeQueryToElementTransformer(signatureAlgorithm, digestAlgorithm, hubEntityId).apply(attributeQuery).getOwnerDocument();
        Document soapResponse = JerseyClientBuilder.createClient().target(uri).request()
                .post(Entity.entity(attributeQueryDocument, TEXT_XML_TYPE))
                .readEntity(Document.class);

        Element soapMessage = new SoapMessageManager().unwrapSoapMessage(soapResponse, SamlElementType.Response);
        return responseElementToOpenSamlXMLObjectTransformer.apply(soapMessage);
    }

    public static AttributeQueryToElementTransformer getAttributeQueryToElementTransformer(SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm, String hubEntityId) {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(TestEntityIds.HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));

        IdaKeyStoreCredentialRetriever privateCredentialFactory = new IdaKeyStoreCredentialRetriever(keyStore);
        return new AttributeQueryToElementTransformer(
                new SigningRequestAbstractTypeSignatureCreator<>(new SignatureFactory(privateCredentialFactory, signatureAlgorithm, digestAlgorithm)),
                new SamlAttributeQueryAssertionSignatureSigner(privateCredentialFactory, new OpenSamlXmlObjectFactory(), hubEntityId),
                new SamlSignatureSigner<>(),
                new XmlObjectToElementTransformer<>(),
                new SamlAttributeQueryAssertionEncrypter(new EncryptionCredentialFactory(new HardCodedKeyStore(hubEntityId)), new EncrypterFactory(), requestId -> TEST_RP_MS)
        );
    }
}
