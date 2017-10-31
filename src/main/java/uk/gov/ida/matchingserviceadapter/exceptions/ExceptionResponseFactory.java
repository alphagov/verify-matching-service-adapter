package uk.gov.ida.matchingserviceadapter.exceptions;

import com.google.inject.Inject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;

public class ExceptionResponseFactory {

    private final OpenSamlXmlObjectFactory factory;
    private final IdaKeyStoreCredentialRetriever credentialFactory;
    private final IdGenerator idGenerator;

    @Inject
    public ExceptionResponseFactory(OpenSamlXmlObjectFactory factory, IdaKeyStoreCredentialRetriever credentialFactory, IdGenerator idGenerator) {
        this.factory = factory;
        this.credentialFactory = credentialFactory;
        this.idGenerator = idGenerator;
    }

    public Element createResponse(String requestId, String issuerId, String message) throws MarshallingException, SignatureException {
        org.opensaml.saml.saml2.core.Response response = factory.createResponse();
        Issuer issuer = factory.createIssuer(issuerId);
        response.setIssuer(issuer);
        response.setInResponseTo(requestId);
        response.setID(idGenerator.getId());

        StatusCode statusCode = factory.createStatusCode();
        statusCode.setValue(StatusCode.REQUESTER);

        Status status = factory.createStatus();
        status.setStatusCode(statusCode);
        response.setStatus(status);

        StatusMessage statusMessage = factory.createStatusMessage();
        statusMessage.setMessage(message);
        status.setStatusMessage(statusMessage);

        Signature signature = factory.createSignature();
        signature.setSigningCredential(credentialFactory.getSigningCredential());
        response.setSignature(signature);

        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(response.getSignature());

        return response.getDOM();
    }
}
