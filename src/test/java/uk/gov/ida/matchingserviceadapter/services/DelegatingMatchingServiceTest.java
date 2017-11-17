package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.controllogic.ServiceLocator;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedEncryptedAssertionContainer;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DelegatingMatchingServiceTest {
    private DelegatingMatchingService service;

    @Mock
    private Document attributeQueryDocument;

    @Mock
    private AttributeQuery attributeQuery;

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private ServiceLocator<MatchingServiceRequestContext, MatchingService> serviceLocator;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        SoapMessageManager soapMessageManager = mock(SoapMessageManager.class);
        ElementToOpenSamlXMLObjectTransformer transformer = mock(ElementToOpenSamlXMLObjectTransformer.class);

        Element attributeQueryElement = mock(Element.class);
        when(soapMessageManager.unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery)).thenReturn(attributeQueryElement);

        Issuer issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("theIssuerValue");
        when(attributeQuery.getID()).thenReturn("theAttributeQueryId");
        when(attributeQuery.getIssuer()).thenReturn(issuer);
        when(transformer.apply(attributeQueryElement)).thenReturn(attributeQuery);

        service = new DelegatingMatchingService(serviceLocator, soapMessageManager, transformer, assertionDecrypter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void handleReturnsAnErrorWhenNoDelegateIsFound() {
        exception.expectMessage("No delegate found to handle Matching Service Request");

        MatchingServiceRequestContext requestContext = new MatchingServiceRequestContext(attributeQueryDocument);
        service.handle(requestContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void handleSuccessfullyDelegatesToService() {
        MatchingService delegate = mock(MatchingService.class);
        MatchingServiceRequestContext requestContext = new MatchingServiceRequestContext(attributeQueryDocument);
        when(serviceLocator.findServiceFor(requestContext)).thenReturn(delegate);

        service.handle(requestContext);

        assertThat(requestContext.getAttributeQuery(), sameInstance(attributeQuery));
        assertThat(requestContext.getAssertions(), equalTo(Collections.emptyList()));

        verify(serviceLocator).findServiceFor(requestContext);
        verify(assertionDecrypter).decryptAssertions(any(ValidatedEncryptedAssertionContainer.class));
        verify(delegate).handle(requestContext);
        verifyNoMoreInteractions(delegate, serviceLocator, assertionDecrypter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void handleSuccessfullyDecryptsAssertionsInAttributeQuery() {
        MatchingService delegate = mock(MatchingService.class);
        MatchingServiceRequestContext requestContext = new MatchingServiceRequestContext(attributeQueryDocument);
        when(serviceLocator.findServiceFor(requestContext)).thenReturn(delegate);

        Subject subject = mock(Subject.class);
        SubjectConfirmation subjectConfirmation = mock(SubjectConfirmation.class);
        SubjectConfirmationData subjectConfirmationData = mock(SubjectConfirmationData.class);
        EncryptedAssertion encryptedAssertion = mock(EncryptedAssertion.class);
        when(subject.getSubjectConfirmations()).thenReturn(Arrays.asList(subjectConfirmation));
        when(subjectConfirmation.getSubjectConfirmationData()).thenReturn(subjectConfirmationData);
        when(subjectConfirmationData.getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME)).thenReturn(Arrays.asList(encryptedAssertion));
        when(attributeQuery.getSubject()).thenReturn(subject);

        service.handle(requestContext);

        assertThat(requestContext.getAttributeQuery(), sameInstance(attributeQuery));
        assertThat(requestContext.getAssertions(), equalTo(Collections.emptyList()));

        ArgumentCaptor<ValidatedEncryptedAssertionContainer> encryptedAssertionContainerArgumentCaptor = ArgumentCaptor.forClass(ValidatedEncryptedAssertionContainer.class);
        verify(assertionDecrypter).decryptAssertions(encryptedAssertionContainerArgumentCaptor.capture());
        assertThat(encryptedAssertionContainerArgumentCaptor.getValue().getEncryptedAssertions(), equalTo(Arrays.asList(encryptedAssertion)));

        verify(serviceLocator).findServiceFor(requestContext);
        verify(delegate).handle(requestContext);
        verifyNoMoreInteractions(delegate, serviceLocator);
    }
}