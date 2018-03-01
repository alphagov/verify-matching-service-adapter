package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

public abstract class MatchingDatasetDtoBuilder<T extends AddressDto> {

    protected Optional<SimpleMdsValueDto<String>> firstname = fromNullable(SimpleMdsValueDtoBuilder.<String>aSimpleMdsValueDto().withValue("default-first-name").build());
    protected Optional<SimpleMdsValueDto<String>> middleNames = absent();
    protected List<SimpleMdsValueDto<String>> surnames = new ArrayList<>();
    protected Optional<SimpleMdsValueDto<GenderDto>> gender = absent();
    protected Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = absent();
    protected List<T> addressesHistory = new ArrayList<>();
    protected boolean omitAddressesHistory = false;

    public abstract MatchingDatasetDto build();

    public MatchingDatasetDtoBuilder<T> withFirstname(SimpleMdsValueDto<String> firstname) {
        this.firstname = fromNullable(firstname);
        return this;
    }

    public MatchingDatasetDtoBuilder<T> withMiddleNames(SimpleMdsValueDto<String> middleNames) {
        this.middleNames = fromNullable(middleNames);
        return this;
    }

    public MatchingDatasetDtoBuilder<T> addSurname(SimpleMdsValueDto<String> surname) {
        this.surnames.add(surname);
        return this;
    }

    public MatchingDatasetDtoBuilder<T> withGender(SimpleMdsValueDto<GenderDto> gender) {
        this.gender = fromNullable(gender);
        return this;
    }

    public MatchingDatasetDtoBuilder<T> withDateOfBirth(SimpleMdsValueDto<LocalDate> dateOfBirth) {
        this.dateOfBirth = fromNullable(dateOfBirth);
        return this;
    }

    public MatchingDatasetDtoBuilder<T> withAddressHistory(List<T> addressesHistory) {
        this.addressesHistory = addressesHistory;
        return this;
    }

    public MatchingDatasetDtoBuilder<T> omittingTheAddressHistory() {
        this.omitAddressesHistory = true;
        return this;
    }

    public MatchingDatasetDtoBuilder<T> withSurnameHistory(final List<SimpleMdsValueDto<String>> surnameHistory) {
        this.surnames = surnameHistory;
        return this;
    }
}
