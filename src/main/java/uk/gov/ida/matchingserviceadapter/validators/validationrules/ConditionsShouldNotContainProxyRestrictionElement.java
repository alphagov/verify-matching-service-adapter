package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;

import java.util.function.Predicate;

public class ConditionsShouldNotContainProxyRestrictionElement extends ValidationRule<Conditions> {

    private ConditionsShouldNotContainProxyRestrictionElement() {}

    @Override
    protected Predicate<Conditions> getPredicate() {
        return (conditions) -> conditions.getProxyRestriction() == null;
    }

    @Override
    protected void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain proxy restriction element.");
    }

    public static void validate(Conditions conditions) {
        new ConditionsShouldNotContainProxyRestrictionElement().apply(conditions);
    }
}
