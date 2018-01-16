package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import org.joda.time.LocalDate;

public class EidasMatchingDatasetDto {
    private final EidasAddressDto address;
    private final LocalDate dateOfBirth;
    private final String firstName;
    private final String gender;
    private final String familyName;
    private final String birthName;
    private final String placeOfBirth;

    public EidasMatchingDatasetDto(EidasAddressDto address, LocalDate dateOfBirth, String firstName, String familyName, String birthName, String gender, String placeOfBirth) {
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.gender = gender;
        this.familyName = familyName;
        this.birthName = birthName;
        this.placeOfBirth = placeOfBirth;
    }

    public EidasAddressDto getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthName() {
        return birthName;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }
}
