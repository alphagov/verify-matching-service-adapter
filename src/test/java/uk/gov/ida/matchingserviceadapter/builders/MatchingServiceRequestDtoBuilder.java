package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import static com.google.common.base.Optional.absent;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetDtoBuilder.aMatchingDatasetDto;

public class MatchingServiceRequestDtoBuilder {

    private String hashedPid = "random";
    private Optional<Cycle3DatasetDto> cycle3Dataset = absent();

    public static MatchingServiceRequestDtoBuilder aMatchingServiceRequestDto() {
        return new MatchingServiceRequestDtoBuilder();
    }

    public VerifyMatchingServiceRequestDto buildVerifyMatchingServiceRequestDto() {
        VerifyMatchingDatasetDto matchingDataset = aMatchingDatasetDto().buildVerifyMatchingDatasetDto();

        return new VerifyMatchingServiceRequestDto(
                matchingDataset,
                cycle3Dataset,
                hashedPid,
                "match",
                LevelOfAssuranceDto.LEVEL_2);
    }

    public MatchingServiceRequestDtoBuilder withHashedPid(String hashedPid) {
        this.hashedPid = hashedPid;
        return this;
    }
}
