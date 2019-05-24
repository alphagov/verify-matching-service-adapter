package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

import java.util.function.Predicate;

public class ConditionsShouldNotContainOneTimeUseElement extends ValidationRule<Conditions> {

    private ConditionsShouldNotContainOneTimeUseElement() {}

    @Override
    protected Predicate<Conditions> getPredicate() {
        return (conditions) -> conditions.getOneTimeUse() == null;
    }

    @Override
    protected void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain one time use element.");
    }

    public static void validate(Conditions conditions) {
        new ConditionsShouldNotContainOneTimeUseElement().apply(conditions);
    }
}
