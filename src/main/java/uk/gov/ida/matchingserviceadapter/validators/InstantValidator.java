package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class InstantValidator {

    private static final Duration MAXIMUM_INSTANT_AGE = Duration.standardMinutes(5);
    private final DateTimeComparator dateTimeComparator;

    @Inject
    public InstantValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validate(DateTime instant, String instantName) {
        Duration age = new Duration(instant, DateTime.now());
        if (age.isLongerThan(MAXIMUM_INSTANT_AGE)) {
            throw new SamlResponseValidationException(String.format("%s is too far in the past %s",
                    instantName,
                    PeriodFormat.getDefault().print(age.toPeriod())));
        }

        if (dateTimeComparator.isAfterSkewedNow(instant)) {
            throw new SamlResponseValidationException(String.format("%s is in the future %s",
                    instantName,
                    instant.withZone(UTC).toString(dateHourMinuteSecond())));
        }
    }
}
