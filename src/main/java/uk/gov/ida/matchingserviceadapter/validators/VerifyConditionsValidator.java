package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsShouldNotContainOneTimeUseElement;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsShouldNotContainProxyRestrictionElement;


public class VerifyConditionsValidator implements ConditionsValidator {

    private final AssertionTimeRestrictionValidator timeRestrictionValidator;
    private final AudienceRestrictionValidator audienceRestrictionValidator;

    @Inject
    public VerifyConditionsValidator(
            AssertionTimeRestrictionValidator timeRestrictionValidator,
            AudienceRestrictionValidator audienceRestrictionValidator
    ) {
        this.timeRestrictionValidator = timeRestrictionValidator;
        this.audienceRestrictionValidator = audienceRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String entityId) {

        ConditionsElementMustNotBeNull.validate(conditionsElement);

        ConditionsShouldNotContainProxyRestrictionElement.validate(conditionsElement.getProxyRestriction());

        ConditionsShouldNotContainOneTimeUseElement.validate(conditionsElement.getOneTimeUse());

        DateTime notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());
        audienceRestrictionValidator.validate(conditionsElement.getAudienceRestrictions(), entityId);
    }
}
