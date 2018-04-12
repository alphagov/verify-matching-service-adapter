package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;
import uk.gov.ida.saml.core.test.validators.SingleCertificateSignatureValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.validation.messages.Messages;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.AssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.CountryAssertionValidator.IDENTITY_ASSERTION_TYPE_NAME;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_INVALID_SIGNATURE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_ISSUER_EMPTY_MESSAGE;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasAttributeQueryValidatorTest {
	private static final String DEFAULT_REQUEST_ID = "request-id";

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private HubAssertionExtractor hubAssertionExtractor;

    private EidasAttributeQueryValidator validator;

    private static Credential verifySigningCredential = new TestCredentialFactory(
            TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT,
            TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY)
            .getSigningCredential();

    private static Credential countrySigningCredential = new TestCredentialFactory(
            TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
            TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY)
            .getSigningCredential();

    @Before
    public void setUp() {
        SingleCertificateSignatureValidator countrySignatureValidator = new SingleCertificateSignatureValidator(countrySigningCredential);
        SingleCertificateSignatureValidator verifySignatureValidator = new SingleCertificateSignatureValidator(verifySigningCredential);
        validator = new EidasAttributeQueryValidator(
            verifySignatureValidator,
            countrySignatureValidator,
            new DateTimeComparator(Duration.ZERO),
            assertionDecrypter,
            hubAssertionExtractor,
            HUB_CONNECTOR_ENTITY_ID);
    }

    private AttributeQueryBuilder aValidAttributeQuery() {
        return anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature().withSigningCredential(verifySigningCredential).build()
            )
            .withId(DEFAULT_REQUEST_ID)
            .withSubject(aSubjectWithEncryptedAssertions(DEFAULT_REQUEST_ID, HUB_ENTITY_ID, anEidasAssertion().build()));
    }

    @Test
    public void shouldValidateAttributeQuerySuccessfully() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(anEidasAssertion().buildUnencrypted());
        final AttributeQuery attributeQuery = aValidAttributeQuery().build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);
        when(hubAssertionExtractor.getNonHubAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldValidateAttributeQueryWithHubAssertionSuccessfully() {
        Assertion decryptedHubAssertion = aCycle3DatasetAssertion("NI", "QQ123456B").buildUnencrypted();
        Assertion[] decryptedAssertions = new Assertion[] {
            anEidasAssertion().buildUnencrypted(),
            decryptedHubAssertion
        };

        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withSubject(aSubjectWithEncryptedAssertions(DEFAULT_REQUEST_ID, HUB_ENTITY_ID,
                anEidasAssertion().build(),
                aCycle3DatasetAssertion("NI", "QQ123456B").build()))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(decryptedAssertions));
        when(hubAssertionExtractor.isHubAssertion(eq(decryptedHubAssertion))).thenReturn(true);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(anEidasAssertion().buildUnencrypted());
        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withIssuer(anIssuer().withIssuerId("").build())
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_ISSUER_EMPTY_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQuerySignatureValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(anEidasAssertion().buildUnencrypted());
        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            TEST_RP_PUBLIC_SIGNING_CERT,
                            TEST_RP_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            ).build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_INVALID_SIGNATURE_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(anEidasAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted());

        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withSubject(aSubjectWithEncryptedAssertions(DEFAULT_REQUEST_ID, HUB_ENTITY_ID, anEidasAssertion().withIssuer(anIssuer().withIssuerId("").build()).build()))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(IDENTITY_ASSERTION_TYPE_NAME))).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionIsMissing() {
        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withSubject(null)
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList());
        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EidasAttributeQueryValidator.DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryContainsAnEmptySubjectConfirmation() {
        final AttributeQuery attributeQuery = aValidAttributeQuery()
            .withSubject(aSubject().withSubjectConfirmation(null).build())
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList());
        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EidasAttributeQueryValidator.DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationAndEncryptedAssertionValidationBothFail() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(anEidasAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted());

        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId("").build())
            .withSubject(aSubjectWithEncryptedAssertions(DEFAULT_REQUEST_ID, HUB_ENTITY_ID, anEidasAssertion().withIssuer(anIssuer().withIssuerId("").build()).build()))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_ISSUER_EMPTY_MESSAGE)).isTrue();
        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(IDENTITY_ASSERTION_TYPE_NAME))).isTrue();
    }

    private Subject aSubjectWithEncryptedAssertions(final String requestId,
                                                    final String hubEntityId,
                                                    final EncryptedAssertion... encryptedAssertions) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();
        final SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData()
            .withInResponseTo(requestId)
            .addEncryptedAssertions(Arrays.asList(encryptedAssertions))
            .build();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    private AssertionBuilder anEidasAssertion(){
        return AssertionBuilder.anEidasAssertion().withSignature(aSignature().withSigningCredential(countrySigningCredential).build());
    }

    private AssertionBuilder aCycle3DatasetAssertion(String name, String value){
        return AssertionBuilder.aCycle3DatasetAssertion(name, value).withSignature(aSignature().withSigningCredential(verifySigningCredential).build());
    }
}
