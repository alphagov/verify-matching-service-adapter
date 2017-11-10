package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import uk.gov.ida.validation.messages.Messages;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_CONDITIONS_MUST_CONTAIN_ONE_AUDIENCE_RESTRICTION_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_CURRENT_TIME_BEFORE_VALID_TIME_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_CURRENT_TIME_IS_ON_AND_AFTER_VALID_TIME_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_NOT_BEFORE_AND_NOT_ON_OR_AFTER_ARE_MISSING_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_NOT_BEFORE_MUST_BE_LESS_THAN_NOT_ON_OR_AFTER_TIME_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator.DEFAULT_REQUIRED_MESSAGE;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class ConditionsValidatorTest {
    private static final DateTime NOW = DateTime.now(DateTimeZone.UTC);
    private static final String AUDIENCE_URI = "audienceUri";
    private ConditionsValidator<Conditions> validator;
    private AudienceRestriction audienceRestriction;

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(NOW.getMillis());
        validator = new ConditionsValidator<>(conditions -> conditions, AUDIENCE_URI);

        audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(AUDIENCE_URI);
        audienceRestriction.getAudiences().add(audience);
    }

    @After
    public void teardown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldReturnErrorIfConditionsIsNotFound() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_REQUIRED_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfNotBeforeIsNotFoundInConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotOnOrAfter(NOW.plusMillis(1));
        conditions.getAudienceRestrictions().add(audienceRestriction);

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnErrorIfNotBeforeIsNotMet() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(NOW.plusMillis(1));

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_CURRENT_TIME_BEFORE_VALID_TIME_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfNotOnOrAfterIsNotFoundInConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(NOW);
        conditions.getAudienceRestrictions().add(audienceRestriction);

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnErrorIfNotOnOrAfterIsNotMet() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotOnOrAfter(NOW.minusMillis(1));

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_CURRENT_TIME_IS_ON_AND_AFTER_VALID_TIME_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfNotOnOrAfterIsSameAsCurrentTime() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotOnOrAfter(NOW);

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_CURRENT_TIME_IS_ON_AND_AFTER_VALID_TIME_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfNotBeforeIsSameAsNotOnOrAfter() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(NOW);
        conditions.setNotOnOrAfter(NOW);

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_NOT_BEFORE_MUST_BE_LESS_THAN_NOT_ON_OR_AFTER_TIME_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfNotBeforeAndNotOnOrAfterAreNotFoundInConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_NOT_BEFORE_AND_NOT_ON_OR_AFTER_ARE_MISSING_MESSAGE)).isTrue();
    }

    @Test
    public void shouldReturnErrorIfAudienceRestrictionIsNotFoundInConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(NOW);
        conditions.setNotOnOrAfter(NOW.plusMillis(1));

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(DEFAULT_CONDITIONS_MUST_CONTAIN_ONE_AUDIENCE_RESTRICTION_MESSAGE)).isTrue();
    }

    @Test
    public void shouldValidateConditionsSuccessfully() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(NOW);
        conditions.setNotOnOrAfter(NOW.plusMillis(1));
        conditions.getAudienceRestrictions().add(audienceRestriction);

        Messages messages = validator.validate(conditions, messages());

        assertThat(messages.size()).isEqualTo(0);
    }
}
