package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.OneTimeUse;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

public class ConditionsShouldNotContainOneTimeUseElement extends ValidationRule<OneTimeUse> {
    public ConditionsShouldNotContainOneTimeUseElement() {
        super((e) -> e == null);
    }

    public static void validate(OneTimeUse oneTimeUse) {
        new ConditionsShouldNotContainOneTimeUseElement().apply(oneTimeUse);
    }

    public void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain one time use element.");
    }
}
