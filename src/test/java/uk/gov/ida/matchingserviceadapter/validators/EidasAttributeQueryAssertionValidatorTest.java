package uk.gov.ida.matchingserviceadapter.validators;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.AbstractValidator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_INSTANT_IN_FUTURE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateWrongNumberOfAuthnStatementsMessage;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasAttributeQueryAssertionValidatorTest {

    public static final String TYPE_OF_ASSERTION = "Identity";
    @Mock
    private MetadataResolver metadataResolver;

    @Mock
    private CertificateValidator certificateValidator;

    @Mock
    private CertificateExtractor certificateExtractor;

    @Mock
    private EntityDescriptor entityDescriptor;

    private X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private EidasAttributeQueryAssertionValidator validator;
    private static final Duration TTL = Duration.parse("PT999M");
    private static final Duration CLOCK_DELTA = Duration.parse("PT9M");

    @Before
    public void setUp() throws Exception {
        validator = new EidasAttributeQueryAssertionValidator(
            metadataResolver,
            certificateValidator,
            certificateExtractor,
            x509CertificateFactory,
            new DateTimeComparator(org.joda.time.Duration.ZERO),
            TYPE_OF_ASSERTION,
            TTL,
            CLOCK_DELTA);
    }

    @Test
    public void shouldValidateIssuer() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateSubject() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion().withSubject(null).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(SUBJECT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldValidateSignature() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion().addAuthnStatement(anAuthnStatement().build()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorIfThereIsMoreThanOneAuthnStatement() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion()
            .addAuthnStatement(anAuthnStatement().build())
            .addAuthnStatement(anAuthnStatement().build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateWrongNumberOfAuthnStatementsMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfThereAreNoAuthnStatements() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion().buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateWrongNumberOfAuthnStatementsMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateAuthnStatement() throws Exception {
        setUpCertificateValidation();
        Assertion assertion = anAssertion()
            .addAuthnStatement(anAuthnStatement().withAuthnInstant(DateTime.now().plusMinutes(10)).build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrorLike(AUTHN_INSTANT_IN_FUTURE)).isTrue();
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

    private void setUpCertificateValidation() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));
    }
}
