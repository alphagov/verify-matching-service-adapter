package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

public class SimpleMdsValueBuilder<T> {

    public static final DateTime DEFAULT_FROM_DATE = DateTime.parse("2001-01-01");
    public static final DateTime DEFAULT_HISTORICAL_TO_DATE = DateTime.parse("2001-01-01");
    public static final DateTime DEFAULT_HISTORICAL_FROM_DATE = DateTime.parse("2000-01-01");

    private T value;

    private DateTime from;
    private DateTime to;
    private boolean verified = false;

    public static <T> SimpleMdsValueBuilder<T> aCurrentSimpleMdsValue() {
        return new SimpleMdsValueBuilder<T>()
                .withFrom(DEFAULT_FROM_DATE)
                .withTo(null);
    }

    public static <T> SimpleMdsValueBuilder<T> aHistoricalSimpleMdsValue() {
        return new SimpleMdsValueBuilder<T>()
                .withTo(DEFAULT_HISTORICAL_TO_DATE)
                .withFrom(DEFAULT_HISTORICAL_FROM_DATE);
    }

    public SimpleMdsValue<T> build() {
        return new SimpleMdsValue<>(value, from, to, verified);
    }

    public SimpleMdsValueBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public SimpleMdsValueBuilder<T> withFrom(DateTime from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueBuilder<T> withTo(DateTime to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
