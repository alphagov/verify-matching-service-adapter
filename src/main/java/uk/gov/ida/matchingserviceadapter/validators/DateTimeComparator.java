package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DateTimeComparator {

    public DateTimeComparator(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    private final Duration clockSkew;

    public boolean isBeforeNow(DateTime dateTime) {
        return isBefore(dateTime, DateTime.now());
    }

    public boolean isAfterNow(DateTime dateTime) {
        return isBefore(DateTime.now(), dateTime);
    }

    private boolean isBefore(DateTime dateTime, DateTime target) {
        return dateTime.isBefore(target.plus(clockSkew));
    }
}
