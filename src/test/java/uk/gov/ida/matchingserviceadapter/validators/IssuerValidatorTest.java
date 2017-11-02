package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.validation.messages.Messages;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.IssuerValidator.DEFAULT_EMPTY_VALUE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.IssuerValidator.DEFAULT_REQUIRED_MESSAGE;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class IssuerValidatorTest {
    private IssuerValidator<Issuer> validator;

    @Before
    public void setUp() throws Exception {
        validator = new IssuerValidator<>(issuer -> issuer);
    }

    @Test
    public void shouldReturnErrorIfAnIssuerIsNotFound() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAnIssuerIsEmpty() {
        Issuer issuer = anIssuer().withIssuerId("").build();

        Messages messages = validator.validate(issuer, messages());

        assertThat(messages.hasErrorLike(DEFAULT_EMPTY_VALUE_MESSAGE)).isTrue();
    }
}
