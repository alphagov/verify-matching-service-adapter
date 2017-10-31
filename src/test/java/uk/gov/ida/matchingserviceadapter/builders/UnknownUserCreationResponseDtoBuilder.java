package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

public class UnknownUserCreationResponseDtoBuilder {
    private String result = UnknownUserCreationResponseDto.SUCCESS;

    public static UnknownUserCreationResponseDtoBuilder anUnknownUserCreationResponseDto() {
        return new UnknownUserCreationResponseDtoBuilder();
    }

    public UnknownUserCreationResponseDto build() {
        return new UnknownUserCreationResponseDto(result);
    }
}
