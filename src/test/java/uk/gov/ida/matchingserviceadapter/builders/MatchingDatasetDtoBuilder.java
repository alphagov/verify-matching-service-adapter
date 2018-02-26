package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

public class MatchingDatasetDtoBuilder {

    private Optional<SimpleMdsValueDto<String>> firstname = fromNullable(SimpleMdsValueDtoBuilder.<String>aSimpleMdsValueDto().withValue("default-first-name").build());
    private Optional<SimpleMdsValueDto<String>> middleNames = absent();
    private List<SimpleMdsValueDto<String>> surnames = new ArrayList<>();
    private Optional<SimpleMdsValueDto<GenderDto>> gender = absent();
    private Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = absent();
    private List<VerifyAddressDto> addressesHistory = new ArrayList<>();

    public static MatchingDatasetDtoBuilder aMatchingDatasetDto() {
        return new MatchingDatasetDtoBuilder();
    }

    public VerifyMatchingDatasetDto buildVerifyMatchingDatasetDto() {
        return new VerifyMatchingDatasetDto(firstname, middleNames, surnames, gender, dateOfBirth, addressesHistory);
    }

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

    public MatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory) {
        this.addressesHistory = addressesHistory;
        return this;
    }

    public MatchingDatasetDtoBuilder withSurnameHistory(final List<SimpleMdsValueDto<String>> surnameHistory) {
        this.surnames = surnameHistory;
        return this;
    }
}
