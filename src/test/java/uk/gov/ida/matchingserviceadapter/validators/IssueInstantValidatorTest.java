package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.Messages;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator;
import uk.gov.ida.matchingserviceadapter.validators.datetime.NotInTheFutureValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator.NOW_INSTANT_PROVIDER;

public class IssueInstantValidatorTest {
    @Test
    public void ctorTtlFutureMessageDateTimeProviderAndDurations() {
        Message ttlMessage = globalMessage("ttlCode", "ttlMessage");
        Message instantInFutureMessage = globalMessage("inTheFuture", "instantInFutureMessage");
        Instant providedDateTime = Instant.now().minus(1000, DAYS);
        Function<Object, Instant> instantProvider = context -> providedDateTime;
        Duration ttl = Duration.parse("PT60M");
        Duration clockDelta = Duration.parse("PT1M");
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            ttlMessage,
            instantInFutureMessage,
            instantProvider,
            ttl,
            clockDelta
        );

        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.isStopOnFirstError(), is(false));
        assertThat(validator.getValueProvider(), nullValue());

        assertThat(validator.getValidators().length, is(2));
        assertThat(validator.getValidators()[0], instanceOf(DurationNotExceededValidator.class));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getCondition(), nullValue());
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getMessage(), sameInstance(ttlMessage));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getValueProvider(), nullValue());
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getFromInstantInclusiveProvider(), sameInstance(instantProvider));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getToInstantInclusiveProvider(), sameInstance(NOW_INSTANT_PROVIDER));
        assertThat(((DurationNotExceededValidator)validator.getValidators()[0]).getMaximumDuration(), sameInstance(ttl));
        assertThat(validator.getValidators()[1], instanceOf(NotInTheFutureValidator.class));
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getCondition(), nullValue());
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getMessage(), sameInstance(instantInFutureMessage));
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getValueProvider(), nullValue());
        assertThat(((NotInTheFutureValidator)validator.getValidators()[1]).getAtInstantProvider(), sameInstance(instantProvider));

        assertThat(validator.getIssueInstantDateTimeProvider(), sameInstance(instantProvider));
        assertThat(validator.getIssueInstantDateTimeProvider().apply(null), sameInstance(providedDateTime));
        assertThat(validator.getTtl(), sameInstance(ttl));
        assertThat(validator.getClockDelta(), sameInstance(clockDelta));
    }

    @Test
    public void validationDoesNotOccurWhenNoInstantIsProvided() {
        Message ttlMessage = globalMessage("ttlCode", "ttlMessage");
        Message instantInFutureMessage = globalMessage("inTheFuture", "instantInFutureMessage");
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> null;
        Duration ttl = Duration.parse("PT60M");
        Duration clockDelta = Duration.parse("PT1M");
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            ttlMessage,
            instantInFutureMessage,
            instantProvider,
            ttl,
            clockDelta
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(false));

        assertThat(returnedMessages.hasErrorLike(ttlMessage), is(false));
        assertThat(returnedMessages.hasErrorLike(instantInFutureMessage), is(false));
    }

    @Test
    public void validationIsSuccessfulWhenInstantHasNotExpiredAndIsNotInTheFuture() {
        Message ttlMessage = globalMessage("ttlCode", "ttlMessage");
        Message instantInFutureMessage = globalMessage("inTheFuture", "instantInFutureMessage");
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> Instant.now();
        Duration ttl = Duration.parse("PT60M");
        Duration clockDelta = Duration.parse("PT1M");
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            ttlMessage,
            instantInFutureMessage,
            instantProvider,
            ttl,
            clockDelta
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(true));

        assertThat(returnedMessages.hasErrors(), is(false));
    }

    @Test
    public void validationAddsErrorsWhenInstantHasExpiredOrIsInTheFuture() {
        Message ttlMessage = globalMessage("ttlCode", "ttlMessage");
        Message instantInFutureMessage = globalMessage("inTheFuture", "instantInFutureMessage");
        Messages messages = messages();
        Function<Object, Instant> instantProvider = context -> Instant.now().plus(65, MINUTES);
        Duration ttl = Duration.parse("PT60M");
        Duration clockDelta = Duration.parse("PT1M");
        IssueInstantValidator<Object> validator = new IssueInstantValidator<>(
            ttlMessage,
            instantInFutureMessage,
            instantProvider,
            ttl,
            clockDelta
        );

        Messages returnedMessages = validator.validate(new Object(), messages);

        assertThat(returnedMessages, sameInstance(messages));
        assertThat(validator.getCondition(), notNullValue());
        assertThat(validator.getCondition().test(new Object()), is(true));

        assertThat(returnedMessages.hasErrorLike(ttlMessage), is(true));
        assertThat(returnedMessages.hasErrorLike(instantInFutureMessage), is(true));
    }
}