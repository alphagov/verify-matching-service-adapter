package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class ConditionsValidator<T> extends CompositeValidator<T> {
    public static final MessageImpl DEFAULT_REQUIRED_MESSAGE = fieldMessage("conditions", "conditions", "The conditions was not provided.");
    public static final MessageImpl DEFAULT_NOT_BEFORE_AND_NOT_ON_OR_AFTER_ARE_MISSING_MESSAGE = fieldMessage("conditions.notBefore.and.notOnOrAfter", "conditions.notBefore.and.notOnOrAfter.missing", "The NotBefore and NotOnOrAfter were not provided.");
    public static final MessageImpl DEFAULT_CURRENT_TIME_BEFORE_VALID_TIME_MESSAGE = fieldMessage("conditions.notBefore", "conditions.notBefore.invalid", "The NotBefore condition was not met.");
    public static final MessageImpl DEFAULT_CURRENT_TIME_IS_ON_AND_AFTER_VALID_TIME_MESSAGE = fieldMessage("conditions.notOnOrAfter", "conditions.notOnOrAfter.invalid", "The NotOnOrAfter condition was not met.");
    public static final MessageImpl DEFAULT_NOT_BEFORE_MUST_BE_LESS_THAN_NOT_ON_OR_AFTER_TIME_MESSAGE = fieldMessage("conditions.invalid", "conditions.invalid", "The value for NotBefore MUST be less than (earlier than) the value for NotOnOrAfter.");
    public static final MessageImpl DEFAULT_CONDITIONS_MUST_CONTAIN_ONE_AUDIENCE_RESTRICTION_MESSAGE = globalMessage("conditions.audienceRestrictions.audienceRestriction", "There must be 1 audience restriction.");

    public ConditionsValidator(final Function<T, Conditions> valueProvider, final String audienceUri, final DateTimeComparator dateTimeComparator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(DEFAULT_REQUIRED_MESSAGE),
            new FixedErrorValidator<>(conditions -> conditions.getNotBefore() == null && conditions.getNotOnOrAfter() == null, DEFAULT_NOT_BEFORE_AND_NOT_ON_OR_AFTER_ARE_MISSING_MESSAGE),
            new CompositeValidator<>(
                conditions -> conditions.getNotBefore() != null && conditions.getNotOnOrAfter() != null,
                false,
                new FixedErrorValidator<>(conditions -> !conditions.getNotBefore().isBefore(conditions.getNotOnOrAfter()), DEFAULT_NOT_BEFORE_MUST_BE_LESS_THAN_NOT_ON_OR_AFTER_TIME_MESSAGE)
            ),
            new CompositeValidator<>(
                conditions -> conditions.getNotBefore() != null,
                false,
                new FixedErrorValidator<>(conditions -> !dateTimeComparator.isBeforeNow(conditions.getNotBefore()), DEFAULT_CURRENT_TIME_BEFORE_VALID_TIME_MESSAGE)
            ),
            new CompositeValidator<>(
                conditions -> conditions.getNotOnOrAfter() != null,
                false,
                new FixedErrorValidator<>(conditions -> DateTime.now(DateTimeZone.UTC).isAfter(conditions.getNotOnOrAfter()) || DateTime.now(DateTimeZone.UTC).isEqual(conditions.getNotOnOrAfter()), DEFAULT_CURRENT_TIME_IS_ON_AND_AFTER_VALID_TIME_MESSAGE)
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(conditions -> conditions.getAudienceRestrictions().size() != 1, DEFAULT_CONDITIONS_MUST_CONTAIN_ONE_AUDIENCE_RESTRICTION_MESSAGE),
                new AudienceRestrictionValidator<>(conditions -> conditions.getAudienceRestrictions().get(0), audienceUri)
            )
        );
    }
}
