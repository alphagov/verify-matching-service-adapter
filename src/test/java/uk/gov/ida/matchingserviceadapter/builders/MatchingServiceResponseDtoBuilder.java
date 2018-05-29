package uk.gov.ida.matchingserviceadapter.builders;


import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;

public class MatchingServiceResponseDtoBuilder {

    private static final String NO_MATCH = "no-match";
    private static final String MATCH = "match";
    private String result = NO_MATCH;

    public static MatchingServiceResponseDtoBuilder aMatchingServiceResponseDto() {
        return new MatchingServiceResponseDtoBuilder();
    }

    public MatchingServiceResponseDto build() {
        return new MatchingServiceResponseDto(result);
    }

    public MatchingServiceResponseDtoBuilder withNoMatch() {
        result = NO_MATCH;
        return this;
    }

    public MatchingServiceResponseDtoBuilder withMatch() {
        result = MATCH;
        return this;
    }

    public MatchingServiceResponseDtoBuilder withBadResponse() {
        result = "bad-response";
        return this;
    }
}
