package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class MatchingDatasetDto {

    private Optional<SimpleMdsValueDto<String>> firstName = Optional.empty();
    private Optional<SimpleMdsValueDto<String>> middleNames = Optional.empty();
    private List<SimpleMdsValueDto<String>> surnames = new ArrayList<>();
    private Optional<SimpleMdsValueDto<GenderDto>> gender = Optional.empty();
    private Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    protected MatchingDatasetDto() {
    }

    public MatchingDatasetDto(
            Optional<SimpleMdsValueDto<String>> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<SimpleMdsValueDto<String>> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth) {

        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
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

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}