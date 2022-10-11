package uk.gov.ida.matchingserviceadapter.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.slf4j.event.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.services.AttributeQueryService;
import uk.gov.ida.matchingserviceadapter.services.MatchingResponseGenerator;
import uk.gov.ida.matchingserviceadapter.services.UnknownUserResponseGenerator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class AttributeQueryHandlingTests {

    @Mock
    private AttributeQueryService service;

    @Mock
    private SoapMessageManager soapMessageManager;

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private ElementToOpenSamlXMLObjectTransformer<AttributeQuery> unmarshaller;

    @Before
    public void setUp() {
        initMocks(AttributeQueryHandlingTests.class);
    }

    @Test
    public void matchingServiceResourceShouldNotDecryptAssertionsIfSignatureOnAttributeQueryInvalid() {
        MatchingServiceResource matchingServiceResource = new MatchingServiceResource(
                service,
                mock(MatchingResponseGenerator.class),
                soapMessageManager,
                unmarshaller,
                mock(MatchingServiceRequestDtoMapper.class),
                mock(MatchingServiceProxy.class),
                mock(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class),
                assertionDecrypter
        );
        Document attributeQueryDocument = setUpMocks();

        try {
            matchingServiceResource.receiveSoapRequest(attributeQueryDocument);
        } catch (SamlOverSoapException ex) {
            // Needed to allow verify call in finally
        } finally {
            verify(assertionDecrypter, times(0)).decryptAssertions(any());
        }
    }

    @Test
    public void unknownUserResourceShouldNotDecryptAssertionsIfSignatureOnAttributeQueryInvalid() {
        UnknownUserAttributeQueryResource unknownUserResource = new UnknownUserAttributeQueryResource(
                null, // Mocking the function is hard and it won't be called in this test
                soapMessageManager,
                mock(UnknownUserResponseGenerator.class),
                unmarshaller,
                service,
                mock(MatchingServiceProxy.class),
                assertionDecrypter
        );
        Document attributeQueryDocument = setUpMocks();

        try {
            unknownUserResource.receiveUnknownUserRequest(attributeQueryDocument);
        } catch (SamlOverSoapException ex) {
            // Needed to allow verify call in finally
        } finally {
            verify(assertionDecrypter, times(0)).decryptAssertions(any());
        }
    }

    private Document setUpMocks() {
        Document attributeQueryDocument = mock(Document.class);
        Element element = mock(Element.class);
        doReturn(element).when(soapMessageManager)
                .unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery);
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        // To help the "MDCHelper"
        doReturn("an-id").when(attributeQuery).getID();
        Issuer issuer = mock(Issuer.class);
        doReturn("an-issuer").when(issuer).getValue();
        doReturn(issuer).when(attributeQuery).getIssuer();
        doReturn(attributeQuery).when(unmarshaller).apply(element);

        doThrow(new SamlTransformationErrorException("Invalid signature mock", Level.INFO))
                .when(service)
                .validate(attributeQuery);
        return attributeQueryDocument;
    }
}