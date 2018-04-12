package uk.gov.ida.matchingserviceadapter.validators;

import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.AbstractValueProvidedValidator;
import uk.gov.ida.validation.validators.Validator;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

/**
 * Filter a collection according to some predicate and apply the supplied validator to the
 * element found which matches the predicate. If no element matches, an error is thrown
 * if the `errorOnMatchFailure` parameter was `true`. If more than one element matches,
 * an error is always thrown.
 */
public class MatchingElementValidator<T, R> extends AbstractValueProvidedValidator<T> {

    public static final MessageImpl NO_VALUE_MATCHING_FILTER = globalMessage("matching.element", "No value matching filter");
    public static final MessageImpl TOO_MANY_MATCHING_FILTER = globalMessage("matching.element", "Too many matching filter");

    private final Predicate<R> filter;
    private final Validator<R> validator;
    private boolean errorOnMatchFailure = true;
    private final Message noValueMessage;
    private final Message tooManyMessage;

    private MatchingElementValidator(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator, boolean errorOnMatchFailure, Message noValueMessage, Message tooManyMessage) {
        super(valueProvider);
        this.filter = matchingFilter;
        this.validator = validator;
        this.errorOnMatchFailure = errorOnMatchFailure;
        this.noValueMessage = noValueMessage;
        this.tooManyMessage = tooManyMessage;
    }

    private MatchingElementValidator(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator, boolean errorOnMatchFailure) {
        this(valueProvider, matchingFilter, validator, errorOnMatchFailure, NO_VALUE_MATCHING_FILTER, TOO_MANY_MATCHING_FILTER);
    }

    public static <T, R> MatchingElementValidator<T, R> failOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator, Message noValueMessage, Message tooManyMessage) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, true, noValueMessage, tooManyMessage);
    }

    public static <T, R> MatchingElementValidator<T, R> failOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, true, NO_VALUE_MATCHING_FILTER, TOO_MANY_MATCHING_FILTER);
    }

    public static <T, R> MatchingElementValidator<T, R> succeedOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator, Message tooManyMessage) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, false, NO_VALUE_MATCHING_FILTER, tooManyMessage);
    }

    public static <T, R> MatchingElementValidator<T, R> succeedOnMatchError(Function<T, Collection<R>> valueProvider, Predicate<R> matchingFilter, Validator<R> validator) {
        return new MatchingElementValidator<>(valueProvider, matchingFilter, validator, false, NO_VALUE_MATCHING_FILTER, TOO_MANY_MATCHING_FILTER);
    }

    @Override
    protected Messages doValidate(T t, Messages messages) {
        Collection<R> toFilter = getValidationValue(t);
        List<R> filtered = toFilter.stream().filter(filter).collect(Collectors.toList());
        if (filtered.size() > 1) {
            return messages.addError(tooManyMessage);
        } else if (filtered.size() < 1) {
            if (errorOnMatchFailure) {
                return messages.addError(noValueMessage);
            } else {
                return messages;
            }
        }
        return validator.validate(filtered.get(0), messages);
    }
}
