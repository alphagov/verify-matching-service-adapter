package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MatchingDatasetDtoBuilder {

    protected Optional<TransliterableMdsValueDto> firstname = Optional.of(new TransliterableMdsValueDto("default-first-name", null));
    protected Optional<SimpleMdsValueDto<String>> middleNames = Optional.empty();
    protected List<TransliterableMdsValueDto> surnames = new ArrayList<>();
    protected Optional<SimpleMdsValueDto<GenderDto>> gender = Optional.empty();
    protected Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = Optional.empty();

    public abstract MatchingDatasetDto build();

    public abstract MatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory);

    public abstract MatchingDatasetDtoBuilder withAddressHistory(Optional<List<UniversalAddressDto>> addressHistory);

    public MatchingDatasetDtoBuilder withFirstname(TransliterableMdsValueDto firstname) {
        this.firstname = Optional.ofNullable(firstname);
        return this;
    }

    public MatchingDatasetDtoBuilder withMiddleNames(SimpleMdsValueDto<String> middleNames) {
        this.middleNames = Optional.ofNullable(middleNames);
        return this;
    }

    public MatchingDatasetDtoBuilder addSurname(TransliterableMdsValueDto surname) {
        this.surnames.add(surname);
        return this;
    }

    public MatchingDatasetDtoBuilder withGender(SimpleMdsValueDto<GenderDto> gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public MatchingDatasetDtoBuilder withDateOfBirth(SimpleMdsValueDto<LocalDate> dateOfBirth) {
        this.dateOfBirth = Optional.ofNullable(dateOfBirth);
        return this;
    }

    public MatchingDatasetDtoBuilder withSurnameHistory(final List<TransliterableMdsValueDto> surnameHistory) {
        this.surnames = surnameHistory;
        return this;
    }
}
