package uk.gov.ida.matchingserviceadapter.validators;

import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.AbstractValueProvidedValidator;
import uk.gov.ida.validation.validators.Validator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

/**
 * Filter a collection according to some predicate and apply the supplied validator to the first
 * element found which matches the predicate
 */
public class MatchingElementValidator<T, R> extends AbstractValueProvidedValidator<T> {

    public static final MessageImpl NO_VALUE_MATCHING_FILTER = globalMessage("subject", "No value matching filter");

    private final Predicate<R> filter;
    private final Validator<R> validator;

    public MatchingElementValidator(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator) {
        super(valueProvider);
        this.filter = matchingFilter;
        this.validator = validator;
    }

    @Override
    protected Messages doValidate(T t, Messages messages) {
        Collection<R> toFilter = getValidationValue(t);
        Optional<R> maybeFirst = toFilter.stream().filter(filter).findFirst();
        if (!maybeFirst.isPresent()) {
            return messages.addError(NO_VALUE_MATCHING_FILTER);
        }
        return validator.validate(maybeFirst.get(), messages);
    }
}
