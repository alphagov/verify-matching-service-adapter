package uk.gov.ida.matchingserviceadapter.validators;

import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.Validator;

import java.util.Objects;
import java.util.function.Function;

public class OptionalValidator<T, R> extends CompositeValidator<T> {

    public OptionalValidator(Function<T, R> valueProvider, Validator<R>... validators) {
        super(Objects::nonNull, true, valueProvider, validators);
    }
}
