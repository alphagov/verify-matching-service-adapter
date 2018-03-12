package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;

public class EidasMatchingDatasetBuilder {

    private String firstName = "a-first-name";
    private String surname = "a-surname";
    private Optional<Gender> gender = Optional.absent();
    private LocalDate dateOfBirth = LocalDate.now().withYear(2000);
    private Optional<Address> address = Optional.absent();

    public static EidasMatchingDatasetBuilder anEidasMatchingDataset() {
        return new EidasMatchingDatasetBuilder();
    }

    public EidasMatchingDataset build() {
        return new EidasMatchingDataset(firstName, surname, gender, dateOfBirth, address);
    }

    public EidasMatchingDatasetBuilder withAddress(Address address) {
        this.address = Optional.of(address);
        return this;
    }

    public EidasMatchingDatasetBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public EidasMatchingDatasetBuilder withSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public EidasMatchingDatasetBuilder withGender(Gender gender) {
        this.gender = Optional.of(gender);
        return this;
    }

    public EidasMatchingDatasetBuilder withDateOfBirth(LocalDate date) {
        this.dateOfBirth = date;
        return this;
    }
}
