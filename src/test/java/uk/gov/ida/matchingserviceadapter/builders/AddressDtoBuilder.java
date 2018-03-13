package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressDtoBuilder {
    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private DateTime fromDate = DateTime.parse("2001-01-01");
    private Optional<DateTime> toDate = Optional.empty();
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

    public UniversalAddressDto buildUniversalAddressDto() {
        return new UniversalAddressDto(
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
        this.postCode = Optional.of(postCode);
        return this;
    }

    public AddressDtoBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.of(internationalPostCode);
        return this;
    }

    public AddressDtoBuilder withUPRN(final String uprn) {
        this.uprn = Optional.of(uprn);
        return this;
    }

    public AddressDtoBuilder withFromDate(final DateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressDtoBuilder withToDate(final DateTime toDate) {
        this.toDate = Optional.of(toDate);
        return this;
    }

    public AddressDtoBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
