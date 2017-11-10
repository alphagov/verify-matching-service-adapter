package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator;
import uk.gov.ida.matchingserviceadapter.validators.datetime.NotInTheFutureValidator;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator.NOW_INSTANT_PROVIDER;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class IssueInstantValidatorTest {
    private static final  Message TTL_MESSAGE = globalMessage("ttlCode", "ttlMessage");
    private static final Message INSTANT_IN_FUTURE_MESSAGE = globalMessage("inTheFuture", "instantInFutureMessage");
    private static final Duration TTL = Duration.parse("PT60M");
    private static final Duration CLOCK_DELTA = Duration.parse("PT1M");

    @Test
    public void ctorTtlFutureMessageDateTimeProviderAndDurations() {
        Instant providedDateTime = Instant.now().minus(1000, DAYS);
        Function<Object, Instant> instantProvider = context -> providedDateTime;
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            TTL_MESSAGE,
            INSTANT_IN_FUTURE_MESSAGE,
            instantProvider,
            TTL,
            CLOCK_DELTA
        );

        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.isStopOnFirstError(), is(false));
        assertThat(validator.getValueProvider(), nullValue());

        assertThat(validator.getValidators().length, is(2));
        assertThat(validator.getValidators()[0], instanceOf(DurationNotExceededValidator.class));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getCondition(), nullValue());
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getMessage(), sameInstance(TTL_MESSAGE));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getValueProvider(), nullValue());
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getFromInstantInclusiveProvider(), sameInstance(instantProvider));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getToInstantInclusiveProvider(), sameInstance(NOW_INSTANT_PROVIDER));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getMaximumDuration(), sameInstance(TTL));
        assertThat(validator.getValidators()[1], instanceOf(NotInTheFutureValidator.class));
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getCondition(), nullValue());
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getMessage(), sameInstance(INSTANT_IN_FUTURE_MESSAGE));
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getValueProvider(), nullValue());
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getAtInstantProvider(), sameInstance(instantProvider));

        assertThat(validator.getIssueInstantDateTimeProvider(), sameInstance(instantProvider));
        assertThat(validator.getIssueInstantDateTimeProvider().apply(null), sameInstance(providedDateTime));
        assertThat(validator.getTtl(), sameInstance(TTL));
        assertThat(validator.getClockDelta(), sameInstance(CLOCK_DELTA));
    }

    @Test
    public void validationDoesNotOccurWhenNoInstantIsProvided() {
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> null;
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            TTL_MESSAGE,
            INSTANT_IN_FUTURE_MESSAGE,
            instantProvider,
            TTL,
            CLOCK_DELTA
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(false));

        assertThat(returnedMessages.hasErrorLike(TTL_MESSAGE), is(false));
        assertThat(returnedMessages.hasErrorLike(INSTANT_IN_FUTURE_MESSAGE), is(false));
    }

    @Test
    public void validationIsSuccessfulWhenInstantHasNotExpiredAndIsNotInTheFuture() {
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> Instant.now();
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            TTL_MESSAGE,
            INSTANT_IN_FUTURE_MESSAGE,
            instantProvider,
            TTL,
            CLOCK_DELTA
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(true));

        assertThat(returnedMessages.hasErrors(), is(false));
    }

    @Test
    public void validationAddsErrorsWhenInstantHasExpiredOrIsInTheFuture() {
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> Instant.now().plus(65, MINUTES);
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            TTL_MESSAGE,
            INSTANT_IN_FUTURE_MESSAGE,
            instantProvider,
            TTL,
            CLOCK_DELTA
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(true));

        assertThat(returnedMessages.hasErrorLike(TTL_MESSAGE), is(true));
        assertThat(returnedMessages.hasErrorLike(INSTANT_IN_FUTURE_MESSAGE), is(true));
    }
}
