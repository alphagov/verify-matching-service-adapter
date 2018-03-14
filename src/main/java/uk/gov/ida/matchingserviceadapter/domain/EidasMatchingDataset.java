package uk.gov.ida.matchingserviceadapter.domain;

import org.joda.time.LocalDate;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;

import java.util.Optional;

public class EidasMatchingDataset {

    private String firstName;
    private String surname;
    private Optional<Gender> gender;
    private LocalDate dateOfBirth;
    private Optional<Address> address;

    public EidasMatchingDataset(String firstName,
                                String surname,
                                Optional<Gender> gender,
                                LocalDate dateOfBirth,
                                Optional<Address> address) {
        this.firstName = firstName;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public Optional<Gender> getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Optional<Address> getAddress() {
        return address;
    }
}
