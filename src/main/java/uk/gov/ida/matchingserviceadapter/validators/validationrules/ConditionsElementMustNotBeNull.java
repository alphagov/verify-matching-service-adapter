package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

import java.util.function.Predicate;

public class ConditionsElementMustNotBeNull extends ValidationRule<Conditions> {

    private ConditionsElementMustNotBeNull() {}

    @Override
    protected Predicate<Conditions> getPredicate() {
        return (conditions) -> conditions != null;
    }

    @Override
    protected void throwException() {
        throw new SamlResponseValidationException("Conditions is missing from the assertion.");
    }

    public static void validate(Conditions conditions) {
        new ConditionsElementMustNotBeNull().apply(conditions);
    }
}
