package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;

import java.util.List;

public class UniversalMatchingDatasetDtoBuilder extends MatchingDatasetDtoBuilder<UniversalAddressDto> {

    public static UniversalMatchingDatasetDtoBuilder aUniversalMatchingDatasetDto() {
        return new UniversalMatchingDatasetDtoBuilder();
    }

    public UniversalMatchingDatasetDto build() {
        Optional<List<UniversalAddressDto>> universalAddressesHistory = omitAddressesHistory ?
                Optional.absent() :
                Optional.of(addressesHistory);
        return new UniversalMatchingDatasetDto(firstname, middleNames, surnames, gender, dateOfBirth, universalAddressesHistory);
    }

}
