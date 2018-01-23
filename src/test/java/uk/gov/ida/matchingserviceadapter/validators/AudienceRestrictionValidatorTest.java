package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import uk.gov.ida.validation.messages.Messages;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.AudienceRestrictionValidator.DEFAULT_AUDIENCES_MUST_CONTAIN_ONE_AUDIENCE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.AudienceRestrictionValidator.DEFAULT_REQUIRED_MESSAGE;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class AudienceRestrictionValidatorTest {
    public static final String AUDIENCE_URI = "audienceUri";
    private AudienceRestrictionValidator<AudienceRestriction> validator;

    @Before
    public void setUp() throws Exception {
        validator = new AudienceRestrictionValidator<>(audienceRestriction -> audienceRestriction, AUDIENCE_URI);
    }

    @Test
    public void shouldReturnErrorIfAudienceRestrictionIsNotFound() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAudiencesInAudienceRestrictionIsNotFound() {
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();

        Messages messages = validator.validate(audienceRestriction, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_AUDIENCES_MUST_CONTAIN_ONE_AUDIENCE_MESSAGE)).isTrue();
    }

    @Test
    public void shouldValidateAudienceRestrictionSuccessfully() {
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(AUDIENCE_URI);
        audienceRestriction.getAudiences().add(audience);

        Messages messages = validator.validate(audienceRestriction, messages());

        assertThat(messages.size()).isEqualTo(0);
    }
}
