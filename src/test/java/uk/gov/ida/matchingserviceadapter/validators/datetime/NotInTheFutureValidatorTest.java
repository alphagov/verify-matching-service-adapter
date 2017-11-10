package uk.gov.ida.matchingserviceadapter.validators.datetime;

import org.junit.Test;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class NotInTheFutureValidatorTest {
    public static final MessageImpl DEFAULT_MESSAGE = globalMessage("theCode", "theMessage");

    @Test
    public void ctorMessageAndAtProvider() {
        Object context = new Object();
        Messages messages = messages();
        Function<Object, Instant> atInstantProvider = c -> Instant.now();
        NotInTheFutureValidator<Object> validator = new NotInTheFutureValidator<>(DEFAULT_MESSAGE, atInstantProvider);

        validator.validate(context, messages);

        assertThat(validator.getCondition(), nullValue());
        assertThat(validator.getMessage(), sameInstance(DEFAULT_MESSAGE));
        assertThat(validator.getValidation(), notNullValue());
        assertThat(validator.getValueProvider(), nullValue());
    }

    @Test
    public void validationIsSuccessfulWhenProvidedInstantIsNotInTheFuture() {
        Messages messages = messages();
        NotInTheFutureValidator<Object> validator = new NotInTheFutureValidator<>(DEFAULT_MESSAGE, c -> Instant.now().minus(10, MINUTES));

        Messages returnedMessages = validator.validate(null, messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(false));
    }

    @Test
    public void validationIsSuccessfulWhenProvidedInstantIsInTheFutureButWithinTheClockDelta() {
        Messages messages = messages();
        NotInTheFutureValidator<Object> validator = new NotInTheFutureValidator<>(DEFAULT_MESSAGE, c -> Instant.now().minus(5, MINUTES), Duration.parse("PT6M"));

        Messages returnedMessages = validator.validate(null, messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(false));
    }

    @Test
    public void validationAddsAnErrorWhenProvidedInstantIsInTheFuture() {
        Messages messages = messages();
        NotInTheFutureValidator<Object> validator = new NotInTheFutureValidator<>(DEFAULT_MESSAGE, c -> Instant.now().plus(10, MINUTES));

        Messages returnedMessages = validator.validate(null, messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(returnedMessages.hasErrorLike(DEFAULT_MESSAGE), is(true));
    }
}
