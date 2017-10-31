package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

public class UnknownUserCreationRequestDtoBuilder {
    private String hashPid = "hashPid";

    public static UnknownUserCreationRequestDtoBuilder anUnknnownUserCreationRequestDto() {
        return new UnknownUserCreationRequestDtoBuilder();
    }

    public UnknownUserCreationRequestDto build() {
        return new UnknownUserCreationRequestDto(hashPid, LevelOfAssuranceDto.LEVEL_1);
    }
}
