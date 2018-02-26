package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "verified", "from", "to" })
public class UniversalAddressDto extends AddressDto {
    private DateTime from;
    private Optional<DateTime> to = Optional.absent();

    @SuppressWarnings("unused") // needed for JAXB
    private UniversalAddressDto() {
        super();
    }

    public UniversalAddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            DateTime from,
            Optional<DateTime> to,
            boolean verified) {

        super(lines, postCode, internationalPostCode, uprn, verified);
        this.from = from;
        this.to = to;
    }

    public DateTime getFrom() {
        return from;
    }

    public Optional<DateTime> getTo() {
        return to;
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
