package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;

import javax.inject.Inject;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class AssertionTimeRestrictionValidator {

    private final DateTimeComparator dateTimeComparator;

    @Inject
    public AssertionTimeRestrictionValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validateNotOnOrAfter(DateTime notOnOrAfter) {
        if (dateTimeComparator.isBeforeNow(notOnOrAfter)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid on or after %s",
                notOnOrAfter.withZone(UTC).toString(dateHourMinuteSecond())
            ));
        }
    }

    public void validateNotBefore(DateTime notBefore) {
        if (notBefore != null && dateTimeComparator.isAfterSkewedNow(notBefore)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid before %s",
                notBefore.withZone(UTC).toString(dateHourMinuteSecond())
            ));
        }
    }
}