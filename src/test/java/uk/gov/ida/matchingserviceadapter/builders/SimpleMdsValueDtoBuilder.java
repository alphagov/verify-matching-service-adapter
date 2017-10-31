package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;

public class SimpleMdsValueDtoBuilder<T> {

    private T value = null;

    private DateTime from = DateTime.now().minusDays(5);
    private DateTime to = DateTime.now().plusDays(5);
    private boolean verified = false;

    public static <T> SimpleMdsValueDtoBuilder<T> aSimpleMdsValueDto() {
        return new SimpleMdsValueDtoBuilder<>();
    }

    public SimpleMdsValueDto<T> build() {
        return new SimpleMdsValueDto<>(value, from, to, verified);
    }

    public SimpleMdsValueDtoBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withFrom(DateTime from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withTo(DateTime to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
