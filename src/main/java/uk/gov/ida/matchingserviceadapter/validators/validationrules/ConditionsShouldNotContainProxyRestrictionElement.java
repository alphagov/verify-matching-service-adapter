package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

public class ConditionsShouldNotContainProxyRestrictionElement extends ValidationRule<Conditions> {
    public ConditionsShouldNotContainProxyRestrictionElement() {
        super((conditions) -> conditions.getProxyRestriction() == null);
    }

    public static void validate(Conditions conditions) {
        new ConditionsShouldNotContainProxyRestrictionElement().apply(conditions);
    }

    @Override
    public void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain proxy restriction element.");
    }
}
