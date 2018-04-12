package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.validators.SingleCertificateSignatureValidator;
import uk.gov.ida.validation.messages.Messages;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class CountryAssertionValidatorTest {

    public CountryAssertionValidator validator;

    @Before
    public void setUp() {
        Credential countrySigningCredential = new TestCredentialFactory(TestCertificateStrings.TEST_PUBLIC_CERT, TestCertificateStrings.TEST_PRIVATE_KEY).getSigningCredential();
        SingleCertificateSignatureValidator countrySignatureValidator = new SingleCertificateSignatureValidator(countrySigningCredential);
        validator = new CountryAssertionValidator(
            countrySignatureValidator,
            new DateTimeComparator(Duration.ZERO),
            HUB_CONNECTOR_ENTITY_ID);
    }

    private boolean fieldHasErrors(Messages messages, String field){
        return messages.hasErrorLike(fieldMessage(field, null, null));
    }

    @Test
    public void shouldReturnWithNoErrors(){
        Assertion assertion = anEidasAssertion().buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(fieldHasErrors(messages, "attributeStatements")).isFalse();
        assertThat(fieldHasErrors(messages, "authnStatements")).isFalse();
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldReturnErrorWithZeroAttributeStatements() {
        Assertion assertion = anEidasAssertion()
            .withoutAttributeStatements()
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());
        assertThat(fieldHasErrors(messages, "attributeStatements")).isTrue();
    }

    @Test
    public void shouldReturnErrorWithMoreThanOneAttributeStatement() {
        Assertion assertion = anEidasAssertion()
            .addAttributeStatement(anEidasAttributeStatement().build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isTrue();
        assertThat(fieldHasErrors(messages, "attributeStatements")).isTrue();
    }

    @Test
    public void shouldReturnErrorWithZeroAuthnStatements() {
        Assertion assertion = anEidasAssertion()
            .withoutAuthnStatements()
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isTrue();
        assertThat(fieldHasErrors(messages, "authnStatements")).isTrue();
    }

    @Test
    public void shouldReturnErrorWithMoreThanOneAuthnStatement() {
        Assertion assertion = anEidasAssertion()
            .addAuthnStatement(anEidasAuthnStatement().build())
            .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isTrue();
        assertThat(fieldHasErrors(messages, "authnStatements")).isTrue();
    }

    @Test
    public void shouldReturnErrorWhenAssertionIsMoreThan20MinutesOld(){
        Assertion assertion = anEidasAssertion()
                .withIssueInstant(DateTime.now().minus(Duration.standardMinutes(21)))
                .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isTrue();
        assertThat(fieldHasErrors(messages, "issueInstant")).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAssertionIsMoreThan1MinuteInTheFuture(){
        Assertion assertion = anEidasAssertion()
                .withIssueInstant(DateTime.now().plus(Duration.standardMinutes(2)))
                .buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.hasErrors()).isTrue();
        assertThat(fieldHasErrors(messages, "issueInstant")).isTrue();
    }
}
