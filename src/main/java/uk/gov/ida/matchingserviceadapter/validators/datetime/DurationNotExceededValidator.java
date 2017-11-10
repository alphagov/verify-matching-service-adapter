package uk.gov.ida.matchingserviceadapter.validators.datetime;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.validation.PredicatedValidator;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;


public class DurationNotExceededValidator<T> extends PredicatedValidator<T> {
    public static final Function<?, Instant> NOW_INSTANT_PROVIDER = ignored -> Instant.now();

    private final Function<T, Instant> fromInstantInclusiveProvider;
    private final Function<T, Instant> toInstantInclusiveProvider;
    private final Duration maximumDuration;


    public DurationNotExceededValidator(@NotNull final Message message,
                                        @NotNull final Function<T, Instant> fromInstantInclusiveProvider,
                                        @NotNull final Function<T, Instant> toInstantInclusiveProvider,
                                        @NotNull final Duration maximumDuration) {
        super(message);
        setValidation(context -> {
            Instant fromInstant = fromInstantInclusiveProvider.apply((T)context);
            Instant toInstant = toInstantInclusiveProvider.apply((T)context);

            return Duration.between(fromInstant, toInstant).abs().compareTo(maximumDuration.abs()) <= 0;
        });
        this.fromInstantInclusiveProvider = fromInstantInclusiveProvider;
        this.toInstantInclusiveProvider = toInstantInclusiveProvider;
        this.maximumDuration = maximumDuration;
    }

    @SuppressWarnings("unchecked")
    public DurationNotExceededValidator(@NotNull final Message message,
                                        @NotNull final Function<T, Instant> fromInstantInclusiveProvider,
                                        @NotNull final Duration maximumDuration) {
        this(message, fromInstantInclusiveProvider, (Function<T, Instant>) NOW_INSTANT_PROVIDER, maximumDuration);
    }

    public Function<T, Instant> getFromInstantInclusiveProvider() {
        return fromInstantInclusiveProvider;
    }

    public Function<T, Instant> getToInstantInclusiveProvider() {
        return toInstantInclusiveProvider;
    }

    public Duration getMaximumDuration() {
        return maximumDuration;
    }
}
