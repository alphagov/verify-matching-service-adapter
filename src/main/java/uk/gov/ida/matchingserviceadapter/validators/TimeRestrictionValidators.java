package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.validators.PredicatedValidator;

import java.util.function.Function;

public class TimeRestrictionValidators {

    public static <T> PredicatedValidator<T> notInFutureValidator(DateTimeComparator dateTimeComparator, Function<T, DateTime> valueProvider, Message message) {
        return new PredicatedValidator<T>(null, valueProvider, message, (DateTime dateTime) -> dateTimeComparator.isBeforeNow(dateTime)) {};
    }

    public static <T> PredicatedValidator<T> notInPastValidator(DateTimeComparator dateTimeComparator, Function<T, DateTime> valueProvider, Message message) {
        return new PredicatedValidator<T>(null, valueProvider, message, (DateTime dateTime) -> dateTimeComparator.isAfterNow(dateTime)) {};
    }

}
