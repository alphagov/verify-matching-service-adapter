package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.validators.datetime.DurationNotExceededValidator;
import uk.gov.ida.matchingserviceadapter.validators.datetime.JodaAndJavaDateTimeConverters;
import uk.gov.ida.matchingserviceadapter.validators.datetime.NotInTheFutureValidator;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.validators.CompositeValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

public class IssueInstantValidator<T> extends CompositeValidator<T> {
    private final Function<T, Instant> issueInstantDateTimeProvider;
    private final Duration ttl;
    private final Duration clockDelta;

    public IssueInstantValidator(Message ttlExpiredMessage,
                                 Message inTheFutureMessage,
                                 Function<T, Instant> issueInstantDateTimeProvider,
                                 Duration ttl,
                                 Duration clockDelta) {
        super(
            context -> issueInstantDateTimeProvider.apply(context) != null,
            false,
            new DurationNotExceededValidator<T>(ttlExpiredMessage, issueInstantDateTimeProvider, ttl),
            new NotInTheFutureValidator<T>(inTheFutureMessage, issueInstantDateTimeProvider, clockDelta)
        );
        this.issueInstantDateTimeProvider = issueInstantDateTimeProvider;
        this.ttl = ttl;
        this.clockDelta = clockDelta;
    }

    public static final <T> IssueInstantValidator<T> IssueInstantJodaDateTimeValidator(Message ttlExpiredMessage,
                                                                                       Message inTheFutureMessage,
                                                                                       Function<T, DateTime> issueInstantDateTimeProvider,
                                                                                       Duration ttl,
                                                                                       Duration clockDelta) {
        return new IssueInstantValidator<T>(
            ttlExpiredMessage,
            inTheFutureMessage,
            JodaAndJavaDateTimeConverters.jodaDataTimeToInstantProvider(issueInstantDateTimeProvider),
            ttl,
            clockDelta
        );
    }

    public Function<T, Instant> getIssueInstantDateTimeProvider() {
        return issueInstantDateTimeProvider;
    }

    public Duration getTtl() {
        return ttl;
    }

    public Duration getClockDelta() {
        return clockDelta;
    }
}
