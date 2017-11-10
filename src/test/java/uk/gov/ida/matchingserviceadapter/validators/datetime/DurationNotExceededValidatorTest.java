package uk.gov.ida.matchingserviceadapter.validators.datetime;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator.NOW_INSTANT_PROVIDER;

public class DurationNotExceededValidatorTest {

    public static final MessageImpl DEFAULT_MESSAGE = globalMessage("theCode", "theMessage");

    @Test
    public void ctorMessageFromToAndMaximum() {
        Message message = globalMessage("theCode", "theMessage");
        Function<Object, Instant> from = c -> Instant.now();
        Function<Object, Instant> to = c -> Instant.now();
        Duration max = Duration.parse("PT10S");
        Instant now = Instant.now();
        DurationNotExceededValidator<Object> validator = new DurationNotExceededValidator<>(message, from, to, max);

        assertThat(validator.getCondition(), nullValue());
        assertThat(validator.getMessage(), sameInstance(message));
        assertThat(validator.getValueProvider(), nullValue());
        assertThat(validator.getValidation(), notNullValue());
        assertThat(validator.getFromInstantInclusiveProvider(), sameInstance(from));
        assertThat(validator.getToInstantInclusiveProvider(), sameInstance(to));
        assertThat(validator.getMaximumDuration(), sameInstance(max));
    }

    @Test
    public void ctorMessageFromAndMaximum() {
        Message message = globalMessage("theCode", "theMessage");
        Function<Object, Instant> from = c -> Instant.now();
        Duration max = Duration.parse("PT10S");
        Instant now = Instant.now();
        DurationNotExceededValidator<Object> validator = new DurationNotExceededValidator<>(message, from, max);

        assertThat(validator.getCondition(), nullValue());
        assertThat(validator.getMessage(), sameInstance(message));
        assertThat(validator.getValueProvider(), nullValue());
        assertThat(validator.getValidation(), notNullValue());
        assertThat(validator.getFromInstantInclusiveProvider(), sameInstance(from));
        assertThat(validator.getToInstantInclusiveProvider(), sameInstance(NOW_INSTANT_PROVIDER));
        assertThat(validator.getMaximumDuration(), sameInstance(max));
    }

    @Test
    public void validationIsSuccessfulWhenDurationIsNotExceeded() {
        Messages messages = messages();
        Instant now = Instant.now();
        DurationNotExceededValidator<Object> validator = new DurationNotExceededValidator<>(DEFAULT_MESSAGE, context -> now.minus(5, MINUTES), context -> now, Duration.parse("PT5M"));

        Messages returnedMessages = validator.validate(null, messages);
        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(false));
    }

    @Test
    public void validationAddsErrorWhenMaximumDurationIsExceeded() {
        Messages messages = messages();
        Instant now = Instant.now();
        DurationNotExceededValidator<Object> validator = new DurationNotExceededValidator<>(DEFAULT_MESSAGE, context -> now.minus(6, MINUTES), context -> now, Duration.parse("PT5M"));

        Messages returnedMessages = validator.validate(null, messages);
        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(true));
    }

    @Test
    public void validationAddsErrorWhenMaximumDurationToNowIsExceeded() {
        Messages messages = messages();
        Instant now = Instant.now();
        DurationNotExceededValidator<Object> validator = new DurationNotExceededValidator<>(DEFAULT_MESSAGE, context -> now.minus(6, MINUTES), Duration.parse("PT5M"));

        Messages returnedMessages = validator.validate(null, messages);
        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(true));
    }
}