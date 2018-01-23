package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeComparatorTest {
    private static final DateTimeComparator comparator = new DateTimeComparator(Duration.standardSeconds(5));

    @Test
    public void isBeforeNowReturnsTrueWhenInThePast() {
        DateTime pastDateTime = DateTime.now().minusMinutes(1);

        assertThat(comparator.isBeforeNow(pastDateTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsTrueWhenInTheFutureButLessThanTheSkew() {
        DateTime dateTime = new DateTime().plusSeconds(1);

        assertThat(comparator.isBeforeNow(dateTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsFalseWhenFarInTheFuture() {
        DateTime dateTime = new DateTime().plusSeconds(10);

        assertThat(comparator.isBeforeNow(dateTime)).isFalse();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInTheFuture() {
        DateTime futureDateTime = DateTime.now().plusMinutes(1);

        assertThat(comparator.isAfterNow(futureDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInThePastButLessThanTheSkew() {
        DateTime pastDateTime = DateTime.now().minusSeconds(1);

        assertThat(comparator.isAfterNow(pastDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsFalseWhenFarInThePast() {
        DateTime pastDateTime = DateTime.now().minusSeconds(10);

        assertThat(comparator.isAfterNow(pastDateTime)).isFalse();
    }
}