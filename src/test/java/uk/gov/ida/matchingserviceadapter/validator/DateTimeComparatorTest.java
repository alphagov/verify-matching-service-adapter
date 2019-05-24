package uk.gov.ida.matchingserviceadapter.validator;


import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;


import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeComparatorTest {

    private static final DateTime baseTime = new DateTime(2017, 1, 1, 12, 0);
    private static final DateTimeComparator comparator = new DateTimeComparator(Duration.standardSeconds(5));

    @Test
    public void isAfterFuzzyReturnsTrueWhenAIsAfterB() throws Exception {
        DateTime newTime = baseTime.plusMinutes(1);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isAfterFuzzyReturnsFalseWhenAIsBeforeB() throws Exception {
        DateTime newTime = baseTime.minusMinutes(1);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    public void isAfterFuzzyReturnsTrueWhenAIsAfterBWithinSkew() throws Exception {
        DateTime newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isAfterFuzzyReturnsTrueWhenAIsBeforeBWithinSkew() throws Exception {
        DateTime newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }


    @Test
    public void isBeforeFuzzyReturnsTrueWhenAIsBeforeB() throws Exception {
        DateTime newTime = baseTime.minusMinutes(1);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isBeforeFuzzyReturnsFalseWhenAIsAfterB() throws Exception {
        DateTime newTime = baseTime.plusMinutes(1);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    public void isBeforeFuzzyReturnsTrueWhenAIsBeforeBWithinSkew() throws Exception {
        DateTime newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isBeforeFuzzyReturnsTrueWhenAIsAfterBWithinSkew() throws Exception {
        DateTime newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }


    @Test
    public void isBeforeNowReturnsTrueWhenInThePast() {
        DateTime pastDateTime = new DateTime().minusMinutes(1);

        assertThat(comparator.isBeforeNow(pastDateTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsTrueWhenInThePastWithinSkew() {
        DateTime pastDateTime = new DateTime().minusSeconds(2);

        assertThat(comparator.isBeforeNow(pastDateTime)).isTrue();
    }


    @Test
    public void isBeforeNowReturnsTrueWhenInTheFutureWithinSkew() {
        DateTime futureDateTime = new DateTime().plusSeconds(2);

        assertThat(comparator.isBeforeNow(futureDateTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsFalseWhenInTheFuture() {
        DateTime futureDateTime = new DateTime().plusMinutes(1);

        assertThat(comparator.isBeforeNow(futureDateTime)).isFalse();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInTheFuture() {
        DateTime futureDateTime = new DateTime().plusMinutes(1);

        assertThat(comparator.isAfterNow(futureDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInTheFutureWithinSkew() {
        DateTime futureDateTime = new DateTime().plusSeconds(2);

        assertThat(comparator.isAfterNow(futureDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInThePastWithinSkew() {
        DateTime pastDateTime = new DateTime().minusSeconds(2);

        assertThat(comparator.isAfterNow(pastDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsFalseWhenInThePast() {
        DateTime pastDateTime = new DateTime().minusMinutes(1);

        assertThat(comparator.isAfterNow(pastDateTime)).isFalse();
    }

    @Test
    public void isAfterSkewedNowReturnsTrueWhenInTheFuture(){
        DateTime futureDateTime = new DateTime().plusMinutes(1);

        assertThat(comparator.isAfterSkewedNow(futureDateTime)).isTrue();
    }

    @Test
    public void isAfterSkewedNowReturnsFalseWhenInTheFutureWithinSkew(){
        DateTime futureDateTime = new DateTime().plusSeconds(2);

        assertThat(comparator.isAfterSkewedNow(futureDateTime)).isFalse();
    }

    @Test
    public void isAfterSkewedNowReturnsFalseWhenInThePast(){
        DateTime pastDateTime = new DateTime().minusMinutes(1);

        assertThat(comparator.isAfterSkewedNow(pastDateTime)).isFalse();
    }

    @Test
    public void isAfterSkewedNowReturnsFalseWhenInThePastWithinSkew(){
        DateTime pastDateTime = new DateTime().minusSeconds(2);

        assertThat(comparator.isAfterSkewedNow(pastDateTime)).isFalse();
    }

    @Test
    public void isBeforeSkewedNowReturnsFalseWhenInTheFuture(){
        DateTime futureDateTime = new DateTime().plusMinutes(1);

        assertThat(comparator.isBeforeSkewedNow(futureDateTime)).isFalse();
    }

    @Test
    public void isBeforeSkewedNowReturnsFalseWhenInTheFutureWithinSkew(){
        DateTime futureDateTime = new DateTime().plusSeconds(2);

        assertThat(comparator.isBeforeSkewedNow(futureDateTime)).isFalse();
    }

    @Test
    public void isBeforeSkewedNowReturnsTrueWhenInThePast(){
        DateTime pastDateTime = new DateTime().minusMinutes(1);

        assertThat(comparator.isBeforeSkewedNow(pastDateTime)).isTrue();
    }

    @Test
    public void isBeforeSkewedNowReturnsFalseWhenInThePastWithinSkew(){
        DateTime pastDateTime = new DateTime().minusSeconds(2);

        assertThat(comparator.isBeforeSkewedNow(pastDateTime)).isFalse();
    }
}
