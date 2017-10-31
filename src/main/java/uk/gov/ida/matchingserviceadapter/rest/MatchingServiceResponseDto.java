package uk.gov.ida.matchingserviceadapter.rest;

public class MatchingServiceResponseDto {
    public static final String MATCH = "match";
    public static final String NO_MATCH = "no-match";
    public static final MatchingServiceResponseDto MATCH_RESPONSE = new MatchingServiceResponseDto(MATCH);

    private String result;

    @SuppressWarnings("unused")//Needed by JAXB
    private MatchingServiceResponseDto() {}

    public MatchingServiceResponseDto(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

}
