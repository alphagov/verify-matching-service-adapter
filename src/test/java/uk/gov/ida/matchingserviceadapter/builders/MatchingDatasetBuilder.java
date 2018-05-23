package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.AddressFactory;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public class MatchingDatasetBuilder {

    private List<SimpleMdsValue<String>> firstnames = new ArrayList<>();
    private List<SimpleMdsValue<String>> middleNames = new ArrayList<>();
    private List<SimpleMdsValue<String>> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private List<SimpleMdsValue<LocalDate>> dateOfBirths = new ArrayList<>();
    private List<Address> currentAddresses = new ArrayList<>();
    private List<Address> previousAddresses = new ArrayList<>();
    private String personalId = "a-personal-id";

    public static MatchingDatasetBuilder aMatchingDataset() {
        return new MatchingDatasetBuilder();
    }

    public static MatchingDatasetBuilder aFullyPopulatedMatchingDataset() {
        final List<Address> currentAddressList = asList(new AddressFactory().create(asList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        final List<Address> previousAddressList = asList(new AddressFactory().create(asList("previous-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        final SimpleMdsValue<String> currentSurname = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-currentSurname").withVerifiedStatus(true).build();
        return aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .addMiddleNames(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(asList(currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aSimpleMdsValue().withValue(LocalDate.parse("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(currentAddressList)
                .withPreviousAddresses(previousAddressList);
    }

    public MatchingDataset build() {
        return new MatchingDataset(firstnames, middleNames, surnames, gender, dateOfBirths, currentAddresses, previousAddresses, personalId);
    }

    public MatchingDatasetBuilder addFirstname(SimpleMdsValue<String> firstname) {
        this.firstnames.add(firstname);
        return this;
    }

    public MatchingDatasetBuilder addMiddleNames(SimpleMdsValue<String> middleNames) {
        this.middleNames.add(middleNames);
        return this;
    }

    public MatchingDatasetBuilder addSurname(SimpleMdsValue<String> surname) {
        this.surnames.add(surname);
        return this;
    }

    public MatchingDatasetBuilder withGender(SimpleMdsValue<Gender> gender) {
        this.gender = Optional.of(gender);
        return this;
    }

    public MatchingDatasetBuilder addDateOfBirth(SimpleMdsValue<LocalDate> dateOfBirth) {
        this.dateOfBirths.add(dateOfBirth);
        return this;
    }

    public MatchingDatasetBuilder withCurrentAddresses(List<Address> currentAddresses) {
        this.currentAddresses = currentAddresses;
        return this;
    }

    public MatchingDatasetBuilder withPreviousAddresses(List<Address> previousAddresses) {
        this.previousAddresses = previousAddresses;
        return this;
    }

    public MatchingDatasetBuilder withPersonalId(String personalId) {
        this.personalId = personalId;
        return this;
    }

    public MatchingDatasetBuilder withoutFirstName() {
        this.firstnames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutMiddleName() {
        this.middleNames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutSurname() {
        this.surnames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutDateOfBirth() {
        this.dateOfBirths.clear();
        return this;
    }

    public MatchingDatasetBuilder withSurnameHistory(
            final List<SimpleMdsValue<String>> surnameHistory) {

        this.surnames.clear();
        this.surnames.addAll(surnameHistory);
        return this;
    }
}
