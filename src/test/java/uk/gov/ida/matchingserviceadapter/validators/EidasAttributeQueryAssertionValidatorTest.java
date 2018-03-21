package uk.gov.ida.matchingserviceadapter.validators;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.AbstractValidator;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_INSTANT_IN_FUTURE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateWrongNumberOfAttributeStatementsMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateWrongNumberOfAuthnStatementsMessage;
import static uk.gov.ida.matchingserviceadapter.validators.MatchingElementValidator.NO_VALUE_MATCHING_FILTER;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasAttributeQueryAssertionValidatorTest {
    @Mock
    private MetadataResolver metadataResolver;

    @Mock
    private CertificateValidator certificateValidator;

    @Mock
    private CertificateExtractor certificateExtractor;

    @Mock
    private EntityDescriptor entityDescriptor;

    private static final String TYPE_OF_ASSERTION = "Identity";
    private static final String HUB_CONNECTOR_ENTITY_ID = "hubConnectorEntityId";
    private X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private EidasAttributeQueryAssertionValidator validator;
    private static final Duration TTL = Duration.parse("PT999M");
    private static final Duration CLOCK_DELTA = Duration.parse("PT9M");

    @Before
    public void setUp() throws Exception {
        validator = new EidasAttributeQueryAssertionValidator(
            metadataResolver,
            certificateExtractor,
            x509CertificateFactory,
            new DateTimeComparator(org.joda.time.Duration.ZERO),
            TYPE_OF_ASSERTION,
            HUB_CONNECTOR_ENTITY_ID,
            TTL,
            CLOCK_DELTA);
    }

    @Test
    public void shouldValidateIssuer() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anEidasAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateSubject() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));
        Assertion assertion = anAssertion().withSubject(null).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(SUBJECT_NOT_PRESENT)).isTrue();
    }

    public void shouldGenerateErrorIfWrongNumberOfAttributeStatements() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anEidasAssertion().addAttributeStatement(anAttributeStatement().build()).withConditions(aConditions()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateWrongNumberOfAttributeStatementsMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateAttributeStatement() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion()
            .addAttributeStatement(anAttributeStatement()
                .build())
            .addAuthnStatement(anAuthnStatement().build()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldValidateSignature() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anEidasAssertion().withConditions(aConditions()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldValidateIssueInstant() {
        Assertion assertion = anAssertion()
            .withIssueInstant(
                DateTime.now(DateTimeZone.UTC)
                .plus(TTL.toMillis())
                .plus(CLOCK_DELTA.toMillis())
                .plusMillis(10000))
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(validator.isStopOnFirstError()).isFalse();
        List<IssueInstantValidator<Object>> issueInstantValidators = Arrays.stream(validator.getValidators())
            .filter(v -> v instanceof IssueInstantValidator)
            .map(v -> (IssueInstantValidator<Object>)v)
            .collect(Collectors.toList());
        assertThat(issueInstantValidators.size()).isEqualTo(1);
        assertThat(issueInstantValidators.get(0).getValidators().length).isEqualTo(2);

        List<Message> issueInstantMessages = Arrays.stream(issueInstantValidators.get(0).getValidators())
            .filter(v -> v instanceof AbstractValidator)
            .map(v -> ((AbstractValidator) v).getMessage())
            .collect(Collectors.toList());
        assertThat(issueInstantMessages.size()).isEqualTo(2);
        assertThat(messages.hasErrorLike(issueInstantMessages.get(0))).isTrue();
        assertThat(messages.hasErrorLike(issueInstantMessages.get(1))).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfThereIsMoreThanOneAuthnStatement() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));

        Assertion assertion = anAssertion()
            .addAuthnStatement(anAuthnStatement().build())
            .addAuthnStatement(anAuthnStatement().build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(generateWrongNumberOfAuthnStatementsMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfThereAreNoAuthnStatements() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));

        Assertion assertion = anAssertion().buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(generateWrongNumberOfAuthnStatementsMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateAuthnStatement() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));

        Assertion assertion = anAssertion()
            .addAuthnStatement(anAuthnStatement().withAuthnInstant(DateTime.now().plusMinutes(10)).build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(AUTHN_INSTANT_IN_FUTURE)).isTrue();
    }

    @Test
    public void shouldValidateConditions() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion().withConditions(null).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(ConditionsValidator.DEFAULT_REQUIRED_MESSAGE)).isTrue();
    }

    private void setUpCertificateValidation() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));
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
}
