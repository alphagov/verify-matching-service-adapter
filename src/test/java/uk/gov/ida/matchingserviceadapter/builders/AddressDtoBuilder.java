package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.absent;

public class AddressDtoBuilder {
    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = absent();
    private Optional<String> internationalPostCode = absent();
    private Optional<String> uprn = absent();
    private DateTime fromDate = DateTime.parse("2001-01-01");
    private Optional<DateTime> toDate = absent();
    private boolean verified = false;

    public VerifyAddressDto buildVerifyAddressDto() {
        return new VerifyAddressDto(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                fromDate,
                toDate,
                verified);
    }

    public AddressDtoBuilder withLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }

    public AddressDtoBuilder withPostCode(final String postCode) {
        this.postCode = Optional.fromNullable(postCode);
        return this;
    }

    public AddressDtoBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.fromNullable(internationalPostCode);
        return this;
    }

    public AddressDtoBuilder withUPRN(final String uprn) {
        this.uprn = Optional.fromNullable(uprn);
        return this;
    }

    public AddressDtoBuilder withFromDate(final DateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressDtoBuilder withToDate(final DateTime toDate) {
        this.toDate = Optional.fromNullable(toDate);
        return this;
    }

    public AddressDtoBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
