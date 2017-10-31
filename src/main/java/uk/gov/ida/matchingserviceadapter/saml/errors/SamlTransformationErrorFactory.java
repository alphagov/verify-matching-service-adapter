package uk.gov.ida.matchingserviceadapter.saml.errors;

import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;
import uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification;

public final class SamlTransformationErrorFactory {

    private SamlTransformationErrorFactory() {
    }

    public static SamlValidationSpecificationFailure missingIssuer() {
        return new GenericHubProfileValidationSpecification(GenericHubProfileValidationSpecification.MISSING_ISSUER);
    }

    public static SamlValidationSpecificationFailure missingId() {
        return new GenericHubProfileValidationSpecification(GenericHubProfileValidationSpecification.MISSING_ID);
    }

    public static SamlValidationSpecificationFailure unsupportedKey(final String key) {
        return new GenericHubProfileValidationSpecification(GenericHubProfileValidationSpecification.UNSUPPORTED_KEY, key);
    }
}
