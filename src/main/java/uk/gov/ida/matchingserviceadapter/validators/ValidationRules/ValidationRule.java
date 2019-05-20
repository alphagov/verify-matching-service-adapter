package uk.gov.ida.matchingserviceadapter.validators.ValidationRules;

import java.util.function.Predicate;

public abstract class ValidationRule<T> {
    private Predicate<T> predicate;

    protected ValidationRule(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    protected void apply(T subject) {
        if (!predicate.test(subject)) {
            throwException();
        }
    }

    public abstract void throwException();
}
