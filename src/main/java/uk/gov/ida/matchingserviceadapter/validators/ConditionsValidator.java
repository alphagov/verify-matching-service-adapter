package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Conditions;

public interface ConditionsValidator {
    void validate(Conditions conditionsElement, String... acceptableEntityIds);
}
