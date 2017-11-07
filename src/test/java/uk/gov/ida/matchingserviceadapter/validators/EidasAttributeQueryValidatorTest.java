package uk.gov.ida.matchingserviceadapter.validators;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.beanplanet.messages.domain.Messages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.security.AssertionDecrypter;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_INVALID_SIGNATURE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_ISSUER_EMPTY_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.IDENTITY_ASSERTION;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(MockitoJUnitRunner.class)
public class EidasAttributeQueryValidatorTest {
    @Mock
    private MetadataResolver verifyMetadataResolver;

    @Mock
    private MetadataResolver countryMetadataResolver;

    @Mock
    private CertificateValidator verifyCertificateValidator;

    @Mock
    private CertificateValidator countryCertificateValidator;

    @Mock
    private CertificateExtractor certificateExtractor;

    @Mock
    private AssertionDecrypter assertionDecrypter;

    @Mock
    private EntityDescriptor entityDescriptor;

    private X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private EidasAttributeQueryValidator validator;

    @Before
    public void setUp() throws Exception {
        IdaSamlBootstrap.bootstrap();
        validator = new EidasAttributeQueryValidator(
            verifyMetadataResolver,
            countryMetadataResolver,
            verifyCertificateValidator,
            countryCertificateValidator,
            certificateExtractor,
            x509CertificateFactory,
            assertionDecrypter);

        when(verifyMetadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(countryMetadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn((entityDescriptor));
        when(certificateExtractor.extractHubSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(HUB_ENTITY_ID, TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, Certificate.KeyUse.Signing)));
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));
    }

    @Test
    public void shouldValidateAttributeQuerySuccessfully() throws ResolverException {
        final EncryptedAssertion encryptedAssertion = anAssertion().build();
        final Assertion assertion = anAssertion().buildUnencrypted();
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
            .withSubject(aSubjectWithEncryptedAssertion(encryptedAssertion, requestId, HUB_ENTITY_ID))
            .build();
        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(assertion));

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationFails() throws ResolverException {
        final EncryptedAssertion encryptedAssertion = anAssertion().build();
        final Assertion assertion = anAssertion().buildUnencrypted();
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
            .withSubject(aSubjectWithEncryptedAssertion(encryptedAssertion, requestId, HUB_ENTITY_ID))
            .build();
        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(assertion));

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_ISSUER_EMPTY_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQuerySignatureValidationFails() throws ResolverException {
        final EncryptedAssertion encryptedAssertion = anAssertion().build();
        final Assertion assertion = anAssertion().buildUnencrypted();
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
            .withSubject(aSubjectWithEncryptedAssertion(encryptedAssertion, requestId, HUB_ENTITY_ID))
            .build();
        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(assertion));

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_INVALID_SIGNATURE_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionValidationFails() throws ResolverException {
        final EncryptedAssertion encryptedAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).build();
        final Assertion assertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted();
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
            .withSubject(aSubjectWithEncryptedAssertion(encryptedAssertion, requestId, HUB_ENTITY_ID))
            .build();
        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(assertion));


        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(IDENTITY_ASSERTION))).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAnEncryptedAssertionIsMissing() throws ResolverException {
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

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EidasAttributeQueryValidator.DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAttributeQueryContainsAnEmptySubjectConfirmation() throws ResolverException {
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
    public void shouldReturnErrorWhenAttributeQueryIssuerValidationAndEncryptedAssertionValidationBothFail() throws ResolverException {
        final EncryptedAssertion encryptedAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).build();
        final Assertion assertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted();
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
            .withSubject(aSubjectWithEncryptedAssertion(encryptedAssertion, requestId, HUB_ENTITY_ID))
            .build();
        when(assertionDecrypter.decryptAssertions(any())).thenReturn(Arrays.asList(assertion));


        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(2);
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
}
