package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.util.ArrayList;
import java.util.List;

public class VerifyMatchingDatasetDtoBuilder extends MatchingDatasetDtoBuilder {

    private List<VerifyAddressDto> addressHistory = new ArrayList<>();

    public static VerifyMatchingDatasetDtoBuilder aVerifyMatchingDatasetDto() {
        return new VerifyMatchingDatasetDtoBuilder();
    }

    public VerifyMatchingDatasetDto build() {
        return new VerifyMatchingDatasetDto(firstname, middleNames, surnames, gender, dateOfBirth, addressHistory);
    }

    @Override
    public MatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory) {
        this.addressHistory = addressesHistory;
        return this;
    }

    @Override
    public MatchingDatasetDtoBuilder withAddressHistory(Optional<List<UniversalAddressDto>> addressHistory) {
        throw new UnsupportedOperationException("Only VerifyAddressDto is supported");
    }

}
