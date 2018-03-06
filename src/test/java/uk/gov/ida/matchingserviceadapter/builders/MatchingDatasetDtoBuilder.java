package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

public abstract class MatchingDatasetDtoBuilder {

    protected Optional<SimpleMdsValueDto<String>> firstname = fromNullable(SimpleMdsValueDtoBuilder.<String>aSimpleMdsValueDto().withValue("default-first-name").build());
    protected Optional<SimpleMdsValueDto<String>> middleNames = absent();
    protected List<SimpleMdsValueDto<String>> surnames = new ArrayList<>();
    protected Optional<SimpleMdsValueDto<GenderDto>> gender = absent();
    protected Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = absent();

    public abstract MatchingDatasetDto build();

    public abstract MatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory);

    public abstract MatchingDatasetDtoBuilder withAddressHistory(Optional<List<UniversalAddressDto>> addressHistory);

    public MatchingDatasetDtoBuilder withFirstname(SimpleMdsValueDto<String> firstname) {
        this.firstname = fromNullable(firstname);
        return this;
    }

    public MatchingDatasetDtoBuilder withMiddleNames(SimpleMdsValueDto<String> middleNames) {
        this.middleNames = fromNullable(middleNames);
        return this;
    }

    public MatchingDatasetDtoBuilder addSurname(SimpleMdsValueDto<String> surname) {
        this.surnames.add(surname);
        return this;
    }

    public MatchingDatasetDtoBuilder withGender(SimpleMdsValueDto<GenderDto> gender) {
        this.gender = fromNullable(gender);
        return this;
    }

    public MatchingDatasetDtoBuilder withDateOfBirth(SimpleMdsValueDto<LocalDate> dateOfBirth) {
        this.dateOfBirth = fromNullable(dateOfBirth);
        return this;
    }

    public MatchingDatasetDtoBuilder withSurnameHistory(final List<SimpleMdsValueDto<String>> surnameHistory) {
        this.surnames = surnameHistory;
        return this;
    }
}
