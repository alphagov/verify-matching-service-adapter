package uk.gov.ida.matchingserviceadapter.validators.datetime;

import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.validators.PredicatedValidator;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

/**
 * A validator which ensures a provided date/time is not in the future.  The validator may be configured with a clock
 * delta/tolerance to cater for clocks on different machines having differing values for the 'current' time.
 *
 * @param <T> The context under which the date/time to test will be provided to the validator for testing.
 */
public class NotInTheFutureValidator<T> extends PredicatedValidator<T> {
    private final Function<T, Instant> atInstantProvider;

    /**
     * Constructs a validator which ensures the provided date/time is not in the future.
     *
     * @param message the message this validator will add to the error messages if the provided date/time is determined as in the future.
     * @param atInstantProvider a provider of the date/time to be tested.
     */
    public NotInTheFutureValidator(@NotNull final Message message,
                                   @NotNull final Function<T, Instant> atInstantProvider) {
        this(message, atInstantProvider, Duration.ZERO);
    }

    /**
     * Constructs a validator which ensures the provided date/time is not in the future, allowing for variations in clock
     * times around the world with the application of a delta/tolerance.
     *
     * @param message the message this validator will add to the error messages if the provided date/time is determined as in the future.
     * @param atInstantProvider a provider of the date/time to be tested.
     * @param clockDelta a delta/tolerance to allow for clocks on different machines having differing values for the 'current' time.
     */
    public NotInTheFutureValidator(@NotNull final Message message,
                                   @NotNull final Function<T, Instant> atInstantProvider,
                                   @NotNull final Duration clockDelta) {
        super(message, context -> {
            Instant atInstant = atInstantProvider.apply((T)context);

            return !atInstant.isAfter(Instant.now().plus(clockDelta));
        });
        this.atInstantProvider = atInstantProvider;
    }

    public Function<T, Instant> getAtInstantProvider() {
        return atInstantProvider;
    }
}
