package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.AbstractValueProvidedValidator;
import org.beanplanet.validation.Validator;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

/**
 * Filter a collection according to some predicate and apply the supplied validator to the first
 * element found which matches the predicate
 */
public class MatchingElementValidator<T, R> extends AbstractValueProvidedValidator<T> {

    public static final MessageImpl NO_VALUE_MATCHING_FILTER = globalMessage("matching.element", "No value matching filter");

    private final Predicate<R> filter;
    private final Validator<R> validator;
    private boolean errorOnMatchFailure = true;

    private MatchingElementValidator(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator, boolean errorOnMatchFailure) {
        super(valueProvider);
        this.filter = matchingFilter;
        this.validator = validator;
        this.errorOnMatchFailure = errorOnMatchFailure;
    }

    public static <T, R> MatchingElementValidator<T, R> failOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, true);
    }

    public static <T, R> MatchingElementValidator<T, R> succeedOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, false);
    }

    @Override
    protected Messages doValidate(T t, Messages messages) {
        Collection<R> toFilter = getValidationValue(t);
        Optional<R> maybeFirst = toFilter.stream().filter(filter).findFirst();
        if (!maybeFirst.isPresent()) {
            if (errorOnMatchFailure) {
                return messages.addError(NO_VALUE_MATCHING_FILTER);
            } else {
                return messages;
            }
        }
        return validator.validate(maybeFirst.get(), messages);
    }
}
