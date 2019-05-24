package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import java.util.function.Predicate;

public abstract class ValidationRule<T> {

    protected abstract Predicate<T> getPredicate();

    protected void apply(T subject) {
        if (!getPredicate().test(subject)) {
            throwException();
        }
    }

    protected abstract void throwException();
}
