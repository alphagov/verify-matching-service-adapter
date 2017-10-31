package uk.gov.ida.matchingserviceadapter.rest;

public class UnknownUserCreationResponseDto {

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    private String result;

    @SuppressWarnings("unused")//Needed by JAXB
    private UnknownUserCreationResponseDto() {}

    public UnknownUserCreationResponseDto(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }


}
