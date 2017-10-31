package uk.gov.ida.matchingserviceadapter.saml.security;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.core.validation.SamlValidationResponse;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

public class AttributeQuerySignatureValidator {

    private final SamlMessageSignatureValidator samlMessageSignatureValidator;

    @Inject
    public AttributeQuerySignatureValidator(SamlMessageSignatureValidator samlMessageSignatureValidator) {
        this.samlMessageSignatureValidator = samlMessageSignatureValidator;
    }

    public ValidatedAttributeQuery validate(AttributeQuery samlMessage) {
        SamlValidationResponse samlValidationResponse = samlMessageSignatureValidator.validate(samlMessage, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        if (!samlValidationResponse.isOK()) {
            SamlValidationSpecificationFailure failure = samlValidationResponse.getSamlValidationSpecificationFailure();
            if (samlValidationResponse.getCause() != null) {
                throw new SamlTransformationErrorException(failure.getErrorMessage(), samlValidationResponse.getCause(), failure.getLogLevel());
            }
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        return new ValidatedAttributeQuery(samlMessage);
    }
}
