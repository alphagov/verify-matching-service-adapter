package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AddressDto {
    private boolean verified;
    private DateTime fromDate;
    private Optional<DateTime> toDate = Optional.absent();
    private Optional<String> postCode = Optional.absent();
    private List<String> lines;
    private Optional<String> internationalPostCode = Optional.absent();
    private Optional<String> uprn = Optional.absent();

    @SuppressWarnings("unused") // needed for JAXB
    private AddressDto() {
    }

    public AddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            DateTime fromDate,
            Optional<DateTime> toDate,
            boolean verified) {

        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
        this.uprn = uprn;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.verified = verified;
    }

    public List<String> getLines() {
        return lines;
    }

    public Optional<String> getPostCode() {
        return postCode;
    }

    public Optional<String> getInternationalPostCode() {
        return internationalPostCode;
    }

    public Optional<String> getUPRN() {
        return uprn;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public Optional<DateTime> getToDate() {
        return toDate;
    }

    public boolean isVerified() {
        return verified;
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
