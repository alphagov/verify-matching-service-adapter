package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.controllogic.ServiceLocator;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.logging.MdcHelper;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelegatingMatchingService implements MatchingService {
    private ServiceLocator<MatchingServiceRequestContext, MatchingService> matchingServiceLocator;
    private final SoapMessageManager soapMessageManager;
    private final ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller;
    private final AssertionDecrypter assertionDecrypter;

    public DelegatingMatchingService(ServiceLocator<MatchingServiceRequestContext, MatchingService> matchingServiceLocator, SoapMessageManager soapMessageManager, ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller, AssertionDecrypter assertionDecrypter) {
        this.matchingServiceLocator = matchingServiceLocator;
        this.soapMessageManager = soapMessageManager;
        this.attributeQueryUnmarshaller = attributeQueryUnmarshaller;
        this.assertionDecrypter = assertionDecrypter;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext requestContext) {
        AttributeQuery attributeQuery = unwrapAttributeQuery(requestContext.getAttributeQueryDocument());
        requestContext.setAttributeQuery(attributeQuery);
        requestContext.setAssertions(decryptAssertions(attributeQuery));

        MatchingService delegateService = matchingServiceLocator.findServiceFor(requestContext);
        if (delegateService == null) {
            throw new IllegalStateException("No delegate found to handle Matching Service Request");
        }

        return delegateService.handle(requestContext);
    }

    private AttributeQuery unwrapAttributeQuery(Document attributeQueryDocument) {
        Element unwrappedMessage = soapMessageManager.unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery);
        AttributeQuery attributeQuery = attributeQueryUnmarshaller.apply(unwrappedMessage);
        MdcHelper.addContextToMdc(attributeQuery);
        return attributeQuery;
    }

    private List<Assertion> decryptAssertions(AttributeQuery attributeQuery) {
        return assertionDecrypter.decryptAssertions(() -> getEncryptedAssertions(attributeQuery));
    }

    private static List<EncryptedAssertion> getEncryptedAssertions(AttributeQuery attributeQuery) {
        if (attributeQuery.getSubject() == null
            || attributeQuery.getSubject().getSubjectConfirmations().isEmpty()) {
            return Collections.emptyList();
        }

        return (List<EncryptedAssertion>) (List<?>) attributeQuery.getSubject()
            .getSubjectConfirmations()
            .stream()
            .flatMap(s -> s.getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME).stream())
            .collect(Collectors.toList());
    }
}
