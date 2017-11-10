package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.validation.PredicatedValidator;
import org.joda.time.DateTime;

import java.util.function.Function;

public final class TimeRestrictionValidators {
    public static <T> PredicatedValidator<T> notInFutureValidator(DateTimeComparator dateTimeComparator, Function<T, DateTime> valueProvider, Message message) {
        return new PredicatedValidator<T>(null, valueProvider, message, (DateTime dateTime) -> dateTimeComparator.isBeforeNow(dateTime)) {};
    }

    public static <T> PredicatedValidator<T> notInPastValidator(DateTimeComparator dateTimeComparator, Function<T, DateTime> valueProvider, Message message) {
        return new PredicatedValidator<T>(null, valueProvider, message, (DateTime dateTime) -> dateTimeComparator.isAfterNow(dateTime)) {};
    }

}
