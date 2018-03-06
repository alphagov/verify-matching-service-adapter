package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "verified", "fromDate", "toDate" })
public class VerifyAddressDto extends AddressDto {
    private DateTime fromDate;
    private Optional<DateTime> toDate = Optional.absent();

    @SuppressWarnings("unused") // needed for JAXB
    private VerifyAddressDto() {
        super();
    }

    public VerifyAddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            DateTime fromDate,
            Optional<DateTime> toDate,
            boolean verified) {

        super(lines, postCode, internationalPostCode, uprn, verified);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public Optional<DateTime> getToDate() {
        return toDate;
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
