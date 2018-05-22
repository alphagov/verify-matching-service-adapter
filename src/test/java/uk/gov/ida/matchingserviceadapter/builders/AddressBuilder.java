package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_FROM_DATE;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_TO_DATE;

public class AddressBuilder {

    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private DateTime fromDate = DateTime.parse("2001-01-01");
    private Optional<DateTime> toDate = Optional.empty();
    private boolean verified = false;

    public static AddressBuilder aCurrentAddress() {
        return new AddressBuilder();
    }

    public static AddressBuilder aHistoricalAddress() {
        return new AddressBuilder()
                .withFromDate(DEFAULT_HISTORICAL_FROM_DATE)
                .withToDate(DEFAULT_HISTORICAL_TO_DATE);
    }

    public Address build() {
        return new Address(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                fromDate,
                toDate,
                verified);
    }

    public AddressBuilder withLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }

    public AddressBuilder withPostCode(final String postCode) {
        this.postCode = Optional.ofNullable(postCode);
        return this;
    }

    public AddressBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        return this;
    }

    public AddressBuilder withUPRN(final String uprn) {
        this.uprn = Optional.ofNullable(uprn);
        return this;
    }

    public AddressBuilder withFromDate(final DateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressBuilder withToDate(final DateTime toDate) {
        this.toDate = Optional.ofNullable(toDate);
        return this;
    }

    public AddressBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
