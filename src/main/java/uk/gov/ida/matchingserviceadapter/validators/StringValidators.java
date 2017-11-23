package uk.gov.ida.matchingserviceadapter.validators;

import org.apache.commons.lang3.StringUtils;
import org.beanplanet.messages.domain.Message;
import org.beanplanet.validation.PredicatedValidator;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

public class StringValidators {

    public static final Message STRING_VALUE_IS_EMPTY = globalMessage("stringValue", "String value is empty");

    public static <T> PredicatedValidator<T> isNonEmpty(Function<T, String> valueProvider) {
        return new PredicatedValidator<T>(null, valueProvider, STRING_VALUE_IS_EMPTY, (String s) -> StringUtils.isNotBlank(s)) {};
    }

}
