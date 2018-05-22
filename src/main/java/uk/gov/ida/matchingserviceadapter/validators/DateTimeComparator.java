package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DateTimeComparator {

    public DateTimeComparator(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    private final Duration clockSkew;

    public boolean isAfterFuzzy(DateTime source, DateTime target) {
        return source.isAfter(target.minus(clockSkew));
    }

    public boolean isBeforeFuzzy(DateTime source, DateTime target) {
        return source.isBefore(target.plus(clockSkew));
    }

    public boolean isBeforeNow(DateTime dateTime) {
        return isBeforeFuzzy(dateTime, DateTime.now());
    }

    public boolean isAfterNow(DateTime dateTime) {
        return isAfterFuzzy(dateTime, DateTime.now());
    }

    public boolean isAfterSkewedNow(DateTime dateTime){
        return !isBeforeNow(dateTime);
    }

    public boolean isBeforeSkewedNow(DateTime dateTime){
        return !isAfterNow(dateTime);
    }

}
