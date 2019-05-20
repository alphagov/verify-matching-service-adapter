package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

public class ConditionsElementMustNotBeNull extends ValidationRule<Conditions> {

    public ConditionsElementMustNotBeNull() {
        super((e) -> e != null);
    }

    public static void validate(Conditions conditions) {
        new ConditionsElementMustNotBeNull().apply(conditions);
    }

    @Override
    public void throwException() {
        throw new SamlResponseValidationException("Conditions is missing from the assertion.");
    }
}
