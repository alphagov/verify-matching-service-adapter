package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressBuilder {

    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private DateTime fromDate = DateTime.parse("2001-01-01");
    private Optional<DateTime> toDate = Optional.empty();
    private boolean verified = false;

    public static AddressBuilder anAddress() {
        return new AddressBuilder();
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
        this.postCode = Optional.of(postCode);
        return this;
    }

    public AddressBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.of(internationalPostCode);
        return this;
    }

    public AddressBuilder withUPRN(final String uprn) {
        this.uprn = Optional.of(uprn);
        return this;
    }

    public AddressBuilder withFromDate(final DateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressBuilder withToDate(final DateTime toDate) {
        this.toDate = Optional.of(toDate);
        return this;
    }

    public AddressBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
