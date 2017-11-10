package uk.gov.ida.matchingserviceadapter.validators.datetime;

import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.function.Function;

/**
 * A static utility class with transformation functions to convert between Joda and Java 8
 * date/time systems.
 */
public final class JodaAndJavaDateTimeConverters {
    public static final Instant jodaDateTimeToJavaInstant(@NotNull DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Instant.ofEpochMilli(dateTime.toInstant().getMillis());
    }

    public static final <T> Function<T, Instant> jodaDataTimeToInstantProvider(@NotNull Function<T, DateTime> jodaDateTimeProvider) {
        return context -> jodaDateTimeToJavaInstant(jodaDateTimeProvider.apply(context));
    }
}
