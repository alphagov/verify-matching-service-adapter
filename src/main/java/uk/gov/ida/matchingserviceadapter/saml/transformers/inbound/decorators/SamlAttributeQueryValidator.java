package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.matchingserviceadapter.saml.errors.SamlTransformationErrorFactory;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;

public class SamlAttributeQueryValidator {

    public void validate(AttributeQuery request) {
        validateRequest(request);
    }

    private void validateRequest(AttributeQuery request) {
        Issuer issuer = request.getIssuer();
        if (issuer == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingIssuer();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        validateIssuer(issuer);

        if (Strings.isNullOrEmpty(request.getID())) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingId();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateIssuer(Issuer issuer) {
        String issuerId = issuer.getValue();
        if (Strings.isNullOrEmpty(issuerId)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingIssuer();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}
