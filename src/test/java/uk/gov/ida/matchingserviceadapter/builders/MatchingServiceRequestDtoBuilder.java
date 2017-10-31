package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;

import static com.google.common.base.Optional.absent;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetDtoBuilder.aMatchingDatasetDto;

public class MatchingServiceRequestDtoBuilder {

    private String hashedPid = "random";
    private MatchingDatasetDto matchingDataset = aMatchingDatasetDto().build();
    private Optional<Cycle3DatasetDto> cycle3Dataset = absent();

    public static MatchingServiceRequestDtoBuilder aMatchingServiceRequestDto() {
        return new MatchingServiceRequestDtoBuilder();
    }

    public MatchingServiceRequestDto build() {
        return new MatchingServiceRequestDto(
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
