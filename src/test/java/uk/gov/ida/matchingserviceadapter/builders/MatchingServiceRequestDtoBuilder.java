package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.util.Optional;

import static uk.gov.ida.matchingserviceadapter.builders.UniversalMatchingDatasetDtoBuilder.aUniversalMatchingDatasetDto;
import static uk.gov.ida.matchingserviceadapter.builders.VerifyMatchingDatasetDtoBuilder.aVerifyMatchingDatasetDto;

public class MatchingServiceRequestDtoBuilder {

    private String hashedPid = "random";
    private Optional<Cycle3DatasetDto> cycle3Dataset = Optional.empty();

    public static MatchingServiceRequestDtoBuilder aMatchingServiceRequestDto() {
        return new MatchingServiceRequestDtoBuilder();
    }

    public VerifyMatchingServiceRequestDto buildVerifyMatchingServiceRequestDto() {
        VerifyMatchingDatasetDto matchingDataset = aVerifyMatchingDatasetDto().build();

        return new VerifyMatchingServiceRequestDto(
                matchingDataset,
                cycle3Dataset,
                hashedPid,
                "match",
                LevelOfAssuranceDto.LEVEL_2);
    }

    public UniversalMatchingServiceRequestDto buildUniversalMatchingServiceRequestDto() {
        UniversalMatchingDatasetDto matchingDataset = aUniversalMatchingDatasetDto().build();

        return new UniversalMatchingServiceRequestDto(
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
