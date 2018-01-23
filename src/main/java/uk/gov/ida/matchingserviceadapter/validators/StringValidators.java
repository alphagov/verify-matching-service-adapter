package uk.gov.ida.matchingserviceadapter.validators;

import org.apache.commons.lang3.StringUtils;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.validators.PredicatedValidator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class StringValidators {

    public static final Message STRING_VALUE_IS_EMPTY = globalMessage("stringValue", "String value is empty");
    public static final Message STRING_VALUE_NOT_ENUMERATED = globalMessage("stringValue", "String value not one of enumerated values");

    public static <T> PredicatedValidator<T> isNonEmpty(Function<T, String> valueProvider) {
        return new PredicatedValidator<T>(null, valueProvider, STRING_VALUE_IS_EMPTY, (String s) -> StringUtils.isNotBlank(s)) {};
    }

    public static <T> PredicatedValidator<T> isOneOf(Function<T, String> valueProvider, String... validValues) {
        return new PredicatedValidator<T>(null, valueProvider, STRING_VALUE_NOT_ENUMERATED, (String s) -> Arrays.asList(validValues).contains(s));
    }

}
