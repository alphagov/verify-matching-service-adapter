package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.Validator;

import java.util.Objects;
import java.util.function.Function;

public class OptionalValidator<T, R> extends CompositeValidator<T> {

    public OptionalValidator(Function<T, R> valueProvider, Validator<R>... validators) {
        super(Objects::nonNull, true, valueProvider, validators);
    }
}
