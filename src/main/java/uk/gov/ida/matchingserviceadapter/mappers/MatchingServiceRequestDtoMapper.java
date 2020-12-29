package uk.gov.ida.matchingserviceadapter.mappers;

import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

public class MatchingServiceRequestDtoMapper {

    private final MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;
    private final boolean universalMatchingDataset;

    public MatchingServiceRequestDtoMapper(
        MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper,
        boolean universalMatchingDataset) {
        this.matchingDatasetToMatchingDatasetDtoMapper = matchingDatasetToMatchingDatasetDtoMapper;
        this.universalMatchingDataset = universalMatchingDataset;
    }

    public MatchingServiceRequestDto map(String requestId, String hashedPid, AssertionData assertionData) {
        if (universalMatchingDataset) {
            UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(assertionData.getMatchingDataset());
            return new UniversalMatchingServiceRequestDto(
                    matchingDatasetDto,
                    assertionData.getCycle3Data().map(c3 -> Cycle3DatasetDto.createFromData(c3.getAttributes())),
                    hashedPid,
                    requestId,
                    AuthnContextToLevelOfAssuranceDtoMapper.map(assertionData.getLevelOfAssurance()));
        }
        else {
            VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(assertionData.getMatchingDataset());
            return new VerifyMatchingServiceRequestDto(
                    matchingDatasetDto,
                    assertionData.getCycle3Data().map(c3 -> Cycle3DatasetDto.createFromData(c3.getAttributes())),
                    hashedPid,
                    requestId,
                    AuthnContextToLevelOfAssuranceDtoMapper.map(assertionData.getLevelOfAssurance()));
        }
    }
}
