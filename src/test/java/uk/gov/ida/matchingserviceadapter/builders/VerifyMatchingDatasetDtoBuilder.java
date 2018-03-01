package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

public class VerifyMatchingDatasetDtoBuilder extends MatchingDatasetDtoBuilder<VerifyAddressDto> {

    public static VerifyMatchingDatasetDtoBuilder aVerifyMatchingDatasetDto() {
        return new VerifyMatchingDatasetDtoBuilder();
    }

    public VerifyMatchingDatasetDto build() {
        return new VerifyMatchingDatasetDto(firstname, middleNames, surnames, gender, dateOfBirth, addressesHistory);
    }

}
