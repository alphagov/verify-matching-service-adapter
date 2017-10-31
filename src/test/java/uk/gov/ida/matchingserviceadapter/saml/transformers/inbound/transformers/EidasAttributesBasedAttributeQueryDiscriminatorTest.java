package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.SubjectQuery;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedEncryptedAssertionContainer;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EidasAttributesBasedAttributeQueryDiscriminatorTest {

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private MetadataRepository metadataRepository;

    @InjectMocks
    private EidasAttributesBasedAttributeQueryDiscriminator discriminator;

    @Test
    public void shouldNotDecryptAssertionIfVerifyFlow() {
        AttributeQuery attributeQuery = buildAttributeQueryWith2Assertions();

        boolean isEidas = discriminator.test(attributeQuery);

        assertThat(isEidas, is(false));
        verifyNoMoreInteractions(assertionDecrypter, metadataRepository);
    }

    @Test
    public void shouldDecryptAndQueryAssertionIfEidasFlow() {
        final String eidasEntityId = "http://an-eidas-country/entity-id";
        AttributeQuery attributeQuery = buildAttributeQueryWithEidasAssertion();
        Assertion assertion = buildDecryptedAssertion(eidasEntityId);
        when(assertionDecrypter.decryptAssertions(any(ValidatedEncryptedAssertionContainer.class))).thenReturn(asList(assertion));

        boolean isEidas = discriminator.test(attributeQuery);

        assertThat(isEidas, is(false));
        verify(assertionDecrypter).decryptAssertions(any(ValidatedEncryptedAssertionContainer.class));
        verify(metadataRepository).hasMetadataForEntity(eidasEntityId);
        verifyNoMoreInteractions(assertionDecrypter, metadataRepository);
    }

    private AttributeQuery buildAttributeQueryWith2Assertions() {
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        Subject subject = mock(Subject.class);
        SubjectConfirmation subjectConfirmation1 = mock(SubjectConfirmation.class);
        SubjectConfirmation subjectConfirmation2 = mock(SubjectConfirmation.class);
        SubjectConfirmationData subjectConfirmationData1 = mock(SubjectConfirmationData.class);
        SubjectConfirmationData subjectConfirmationData2 = mock(SubjectConfirmationData.class);
        EncryptedAssertion encryptedAssertion1 = mock(EncryptedAssertion.class);
        EncryptedAssertion encryptedAssertion2 = mock(EncryptedAssertion.class);

        when(attributeQuery.getSubject()).thenReturn(subject);
        when(subject.getSubjectConfirmations()).thenReturn(asList(subjectConfirmation1, subjectConfirmation2));
        when(subjectConfirmation1.getSubjectConfirmationData()).thenReturn(subjectConfirmationData1);
        when(subjectConfirmation2.getSubjectConfirmationData()).thenReturn(subjectConfirmationData2);
        when(subjectConfirmationData1.getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME)).thenReturn(asList(encryptedAssertion1));
        when(subjectConfirmationData2.getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME)).thenReturn(asList(encryptedAssertion2));

        return attributeQuery;
    }

    private AttributeQuery buildAttributeQueryWithEidasAssertion() {
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        Subject subject = mock(Subject.class);
        SubjectConfirmation subjectConfirmation1 = mock(SubjectConfirmation.class);
        SubjectConfirmationData subjectConfirmationData1 = mock(SubjectConfirmationData.class);
        EncryptedAssertion encryptedAssertion1 = mock(EncryptedAssertion.class);

        when(attributeQuery.getSubject()).thenReturn(subject);
        when(subject.getSubjectConfirmations()).thenReturn(asList(subjectConfirmation1));
        when(subjectConfirmation1.getSubjectConfirmationData()).thenReturn(subjectConfirmationData1);
        when(subjectConfirmationData1.getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME)).thenReturn(asList(encryptedAssertion1));

        return attributeQuery;
    }

    private Assertion buildDecryptedAssertion(String issuerValue) {
        Assertion assertion = mock(Assertion.class);
        Issuer eidasAssertionIssuer = mock(Issuer.class);
        when(assertion.getIssuer()).thenReturn(eidasAssertionIssuer);
        when(eidasAssertionIssuer.getValue()).thenReturn(issuerValue);

        return assertion;

    }
}