package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_INSTANT_IN_FUTURE;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_INSTANT_TOO_OLD;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES;
import static uk.gov.ida.matchingserviceadapter.validators.AuthnStatementValidator.AUTHN_STATEMENT_NOT_PRESENT;

@RunWith(OpenSAMLMockitoRunner.class)
public class AuthnStatementValidatorTest {

    private AuthnStatementValidator<AuthnStatement> validator;

    @Before
    public void setup() {
        IdaSamlBootstrap.bootstrap();
        validator = new AuthnStatementValidator(identity(), new DateTimeComparator(Duration.ZERO));
    }

    @Test
    public void shouldGenerateNoErrorsWhenAuthnStatementIsValid() {
        Messages messages = validator.validate(AuthnStatementBuilder.anAuthnStatement().withAuthnInstant(DateTime.now().minusMinutes(AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES - 1)).build(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenAuthnInstantIsMissing() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(AUTHN_STATEMENT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAuthnInstantIsInTheFuture() {
        Messages messages = validator.validate(AuthnStatementBuilder.anAuthnStatement().withAuthnInstant(DateTime.now().plusMinutes(10)).build(), messages());

        assertThat(messages.hasErrorLike(AUTHN_INSTANT_IN_FUTURE)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAuthnInstantIsTooFarInThePast() {
        Messages messages = validator.validate(AuthnStatementBuilder.anAuthnStatement().withAuthnInstant(DateTime.now().minusMinutes(AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES + 1)).build(), messages());

        assertThat(messages.hasErrorLike(AUTHN_INSTANT_TOO_OLD)).isTrue();
    }

}