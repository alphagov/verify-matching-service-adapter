package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainOneTimeUseElement;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainProxyRestrictionElement;


public class IdpConditionsValidator implements ConditionsValidator {

    private final AssertionTimeRestrictionValidator timeRestrictionValidator;
    private final AudienceRestrictionValidator audienceRestrictionValidator;

    @Inject
    public IdpConditionsValidator(
            AssertionTimeRestrictionValidator timeRestrictionValidator,
            AudienceRestrictionValidator audienceRestrictionValidator
    ) {
        this.timeRestrictionValidator = timeRestrictionValidator;
        this.audienceRestrictionValidator = audienceRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String entityId) {

        ConditionsElementMustNotBeNull.validate(conditionsElement);

        ConditionsShouldNotContainProxyRestrictionElement.validate(conditionsElement);

        ConditionsShouldNotContainOneTimeUseElement.validate(conditionsElement);

        DateTime notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());
        audienceRestrictionValidator.validate(conditionsElement.getAudienceRestrictions(), entityId);
    }
}
