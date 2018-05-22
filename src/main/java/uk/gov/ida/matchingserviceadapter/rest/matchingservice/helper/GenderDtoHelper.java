package uk.gov.ida.matchingserviceadapter.rest.matchingservice.helper;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;

public class GenderDtoHelper {

    public static final String UNSPECIFIED_VALUE = "Unspecified";

    public static GenderDto convertToVerifyGenderDto(String universalGenderString) {
        if (UNSPECIFIED_VALUE.equals(universalGenderString)) {
            return GenderDto.NOT_SPECIFIED;
        } else {
            return GenderDto.valueOf(universalGenderString.toUpperCase());
        }
    }
}
