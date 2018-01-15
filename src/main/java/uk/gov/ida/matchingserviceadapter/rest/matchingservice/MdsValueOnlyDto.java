package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MdsValueOnlyDto<T> {

    private T value;

    @SuppressWarnings("unused") // needed for JAXB
    public MdsValueOnlyDto() {}

    public MdsValueOnlyDto(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
