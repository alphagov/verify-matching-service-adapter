package uk.gov.ida.matchingserviceadapter.validators.ValidationRules;

import org.opensaml.saml.saml2.core.ProxyRestriction;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

public class ConditionsShouldNotContainProxyRestrictionElement extends ValidationRule<ProxyRestriction> {
    public ConditionsShouldNotContainProxyRestrictionElement() {
        super((e) -> e == null);
    }

    public static void validate(ProxyRestriction proxyRestriction) {
        new ConditionsShouldNotContainProxyRestrictionElement().apply(proxyRestriction);
    }

    @Override
    public void throwException() {
        throw new SamlResponseValidationException("Conditions should not contain proxy restriction element.");
    }
}
