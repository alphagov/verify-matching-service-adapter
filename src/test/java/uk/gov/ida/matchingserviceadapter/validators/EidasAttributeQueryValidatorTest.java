package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.validators.SingleCertificateSignatureValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.validation.messages.Messages;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_INVALID_SIGNATURE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_ISSUER_EMPTY_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.IDENTITY_ASSERTION;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;
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
    public static final String HUB_CONNECTOR_ENTITY_ID = "hubConnectorEntityId";

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private HubAssertionExtractor hubAssertionExtractor;

    private EidasAttributeQueryValidator validator;

    @Before
    public void setUp() {
        Credential countrySigningCredential = new TestCredentialFactory(TestCertificateStrings.TEST_PUBLIC_CERT, TestCertificateStrings.TEST_PRIVATE_KEY).getSigningCredential();
        SingleCertificateSignatureValidator countrySignatureValidator = new SingleCertificateSignatureValidator(countrySigningCredential);
        Credential verifySigningCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
        SingleCertificateSignatureValidator verifySignatureValidator = new SingleCertificateSignatureValidator(verifySigningCredential);
        validator = new EidasAttributeQueryValidator(
            verifySignatureValidator,
            countrySignatureValidator,
            new DateTimeComparator(Duration.ZERO),
            assertionDecrypter,
            hubAssertionExtractor,
            HUB_CONNECTOR_ENTITY_ID);
    }

    @Test
    public void shouldValidateAttributeQuerySuccessfully() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(getAssertionBuilder().buildUnencrypted());
        final String requestId = "request-id";
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withId(requestId)
            .withSubject(aSubjectWithEncryptedAssertion(getAssertionBuilder().build(), requestId, HUB_ENTITY_ID))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);
        when(hubAssertionExtractor.getNonHubAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(getAssertionBuilder().buildUnencrypted());

        final String requestId = "request-id";
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId("").build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withId(requestId)
            .withSubject(aSubjectWithEncryptedAssertion(getAssertionBuilder().build(), requestId, HUB_ENTITY_ID))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_ISSUER_EMPTY_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQuerySignatureValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(getAssertionBuilder().buildUnencrypted());
        final String requestId = "request-id";
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            TEST_RP_PUBLIC_SIGNING_CERT,
                            TEST_RP_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withId(requestId)
            .withSubject(aSubjectWithEncryptedAssertion(getAssertionBuilder().build(), requestId, HUB_ENTITY_ID))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_INVALID_SIGNATURE_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionValidationFails() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(getAssertionBuilder().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted());

        final String requestId = "request-id";
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withId(requestId)
            .withSubject(aSubjectWithEncryptedAssertion(getAssertionBuilder().withIssuer(anIssuer().withIssuerId("").build()).build(), requestId, HUB_ENTITY_ID))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);
        when(hubAssertionExtractor.getNonHubAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(IDENTITY_ASSERTION))).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionIsMissing() {
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withSubject(null)
            .build();

        Messages messages = validator.validate(attributeQuery, messages());
        when(hubAssertionExtractor.getNonHubAssertions(any())).thenReturn(Arrays.asList(anEidasAssertion().withConditions(aConditions()).buildUnencrypted()));

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EidasAttributeQueryValidator.DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryContainsAnEmptySubjectConfirmation() {
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withSubject(aSubject().withSubjectConfirmation(null).build())
            .build();

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EidasAttributeQueryValidator.DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationAndEncryptedAssertionValidationBothFail() {
        List<Assertion> eidasDecryptedAssertions = Arrays.asList(getAssertionBuilder().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted());

        final String requestId = "request-id";
        final AttributeQuery attributeQuery = anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId("").build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .withId(requestId)
            .withSubject(aSubjectWithEncryptedAssertion(getAssertionBuilder().withIssuer(anIssuer().withIssuerId("").build()).build(), requestId, HUB_ENTITY_ID))
            .build();

        when(assertionDecrypter.decryptAssertions(any())).thenReturn(eidasDecryptedAssertions);
        when(hubAssertionExtractor.getNonHubAssertions(any())).thenReturn(eidasDecryptedAssertions);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.hasErrorLike(DEFAULT_ISSUER_EMPTY_MESSAGE)).isTrue();
        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(IDENTITY_ASSERTION))).isTrue();
    }

    private Subject aSubjectWithEncryptedAssertion(final EncryptedAssertion encryptedAssertion,
                                                   final String requestId,
                                                   final String hubEntityId) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();
        final SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().withInResponseTo(requestId).addAssertion(encryptedAssertion).build();
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }


    private Conditions aConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(HUB_CONNECTOR_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AssertionBuilder getAssertionBuilder() {
        return anEidasAssertion().withConditions(aConditions());
    }
}
