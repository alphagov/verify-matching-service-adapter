package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.util.ArrayList;
import java.util.List;

public class EidasMatchingDatasetDtoBuilder {

    private List<VerifyAddressDto> addressHistory = new ArrayList<>();

    public static EidasMatchingDatasetDtoBuilder anEidasMatchingDatasetDto() {
        return new EidasMatchingDatasetDtoBuilder();
    }

    public EidasMatchingDatasetDto build() {
        return new EidasMatchingDatasetDto(null, null, null, null, null, null);  //FIXME
    }

    public EidasMatchingDatasetDtoBuilder withAddressHistory(List<VerifyAddressDto> addressesHistory) {
        this.addressHistory = addressesHistory;
        return this;
    }

    public EidasMatchingDatasetDtoBuilder withAddressHistory(Optional<List<UniversalAddressDto>> addressHistory) {
        throw new UnsupportedOperationException("Only VerifyAddressDto is supported");
    }

}
