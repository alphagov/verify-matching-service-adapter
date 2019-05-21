package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

public class ConditionsShouldNotContainOneTimeUseElement extends ValidationRule<Conditions> {
    public ConditionsShouldNotContainOneTimeUseElement() {
        super((conditions) -> conditions.getOneTimeUse() == null);
    }

    public static void validate(Conditions conditions) {
        new ConditionsShouldNotContainOneTimeUseElement().apply(conditions);
    }

    public void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain one time use element.");
    }
}
