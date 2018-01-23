package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import uk.gov.ida.validation.messages.Messages;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.AudienceValidator.DEFAULT_REQUIRED_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.AudienceValidator.DEFAULT_REQUIRED_URI_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.AudienceValidator.generateMismatchedAudienceUriMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class AudienceValidatorTest {
    public static final String AUDIENCE_URI = "audienceUri";
    private AudienceValidator<Audience> validator;

    @Before
    public void setUp() throws Exception {
        validator = new AudienceValidator<>(audience -> audience, AUDIENCE_URI);
    }

    @Test
    public void shouldReturnErrorIfAudienceIsNotFound() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAudienceUriIsNotFound() {
        Audience audience = new AudienceBuilder().buildObject();
        Messages messages = validator.validate(audience, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_URI_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAudienceUriIsEmpty() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI("");
        Messages messages = validator.validate(audience, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_URI_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAudienceUriDoesNotMatchExpectedEntityId() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI("invalidEntityId");
        Messages messages = validator.validate(audience, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateMismatchedAudienceUriMessage(AUDIENCE_URI))).isTrue();
    }

    @Test
    public void shouldValidateAudienceUriSuccessfully() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(AUDIENCE_URI);
        Messages messages = validator.validate(audience, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }
}
