package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MatchingDatasetDto {

    private Optional<SimpleMdsValueDto<String>> firstName = Optional.absent();
    private Optional<SimpleMdsValueDto<String>> middleNames = Optional.absent();
    private List<SimpleMdsValueDto<String>> surnames = new ArrayList<>();
    private Optional<SimpleMdsValueDto<GenderDto>> gender = Optional.absent();
    private Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = Optional.absent();
    private List<AddressDto> addresses = new ArrayList<>();

    @SuppressWarnings("unused") // needed for JAXB
    private MatchingDatasetDto() {
    }

    public MatchingDatasetDto(
            Optional<SimpleMdsValueDto<String>> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<SimpleMdsValueDto<String>> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth,
            List<AddressDto> addresses) {

        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
    }

    public Optional<SimpleMdsValueDto<String>> getFirstName() {
        return firstName;
    }

    public Optional<SimpleMdsValueDto<String>> getMiddleNames() {
        return middleNames;
    }

    public List<SimpleMdsValueDto<String>> getSurnames() {
        return surnames;
    }

    public Optional<SimpleMdsValueDto<GenderDto>> getGender() {
        return gender;
    }

    public Optional<SimpleMdsValueDto<LocalDate>> getDateOfBirth() {
        return dateOfBirth;
    }

    public List<AddressDto> getAddresses() {
        return addresses;
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