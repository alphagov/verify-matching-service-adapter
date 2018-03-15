package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.List;
import java.util.Optional;

public class UniversalMatchingDatasetDtoBuilder extends MatchingDatasetDtoBuilder {

    private Optional<List<UniversalAddressDto>> addressHistory = Optional.empty();

    public static UniversalMatchingDatasetDtoBuilder aUniversalMatchingDatasetDto() {
        return new UniversalMatchingDatasetDtoBuilder();
    }

    public UniversalMatchingDatasetDto build() {
        return new UniversalMatchingDatasetDto(firstname, middleNames, surnames, gender, dateOfBirth, addressHistory);
    }

    @Override
    public MatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory) {
        throw new UnsupportedOperationException("Only UniversalAddressDto is supported");
    }

    @Override
    public MatchingDatasetDtoBuilder withAddressHistory(Optional<List<UniversalAddressDto>> addressHistory) {
        this.addressHistory = addressHistory;
        return this;
    }

}
