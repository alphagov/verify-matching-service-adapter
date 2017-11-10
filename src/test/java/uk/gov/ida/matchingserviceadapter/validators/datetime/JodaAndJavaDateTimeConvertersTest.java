package uk.gov.ida.matchingserviceadapter.validators.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.time.Instant;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.datetime.JodaAndJavaDateTimeConverters.jodaDataTimeToInstantProvider;
import static uk.gov.ida.matchingserviceadapter.validators.datetime.JodaAndJavaDateTimeConverters.jodaDateTimeToJavaInstant;

public class JodaAndJavaDateTimeConvertersTest {
    @Test
    public void ctorForTestCoverageOnly() {
        new JodaAndJavaDateTimeConverters();
    }

    @Test
    public void jodaDateTimeToJavaInstantWithNull() {
        assertThat(jodaDateTimeToJavaInstant(null), nullValue());
    }

    @Test
    public void jodaDateTimeToJavaInstantSuccessful() {
        DateTime jodaDateTimeUtc = DateTime.now(UTC);
        DateTime jodaDateTimeInDefaultZone = jodaDateTimeUtc.toDateTime(DateTimeZone.getDefault());

        assertThat(jodaDateTimeToJavaInstant(jodaDateTimeInDefaultZone), equalTo(Instant.ofEpochMilli(jodaDateTimeUtc.getMillis())));
    }

    @Test
    public void jodaDataTimeToInstantProviderSuccessful() {
        DateTime jodaDateTimeUtc = DateTime.now(UTC);
        DateTime jodaDateTimeInDefaultZone = jodaDateTimeUtc.toDateTime(DateTimeZone.getDefault());

        Function<Object, Instant> javaInstantProvider = jodaDataTimeToInstantProvider(context -> jodaDateTimeInDefaultZone);

        assertThat(javaInstantProvider.apply(new Object()), equalTo(Instant.ofEpochMilli(jodaDateTimeUtc.getMillis())));
    }
}