package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

import java.util.List;


public class AudienceRestrictionValidator {
    @Inject
    public AudienceRestrictionValidator() {
    }

    public void validate(List<AudienceRestriction> audienceRestrictions, String entityId) {
        if (audienceRestrictions == null || audienceRestrictions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one audience restriction is expected.");
        }

        List<Audience> audiences = audienceRestrictions.get(0).getAudiences();
        if (audiences == null || audiences.size() != 1) {
            throw new SamlResponseValidationException("Exactly one audience is expected.");
        }

        String audience = audiences.get(0).getAudienceURI();
        if (!entityId.equals(audience)) {
            throw new SamlResponseValidationException(String.format("Audience must match entity ID. Expected %s but was %s", entityId, audience));
        }
    }
}
