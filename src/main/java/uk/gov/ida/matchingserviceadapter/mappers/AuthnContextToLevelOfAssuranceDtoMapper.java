package uk.gov.ida.matchingserviceadapter.mappers;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.saml.core.domain.AuthnContext;

public class AuthnContextToLevelOfAssuranceDtoMapper {

    private AuthnContextToLevelOfAssuranceDtoMapper() {}

    public static LevelOfAssuranceDto map(AuthnContext authnContext) {
        switch (authnContext){
            case LEVEL_1:
                return LevelOfAssuranceDto.LEVEL_1;
            case LEVEL_2:
                return LevelOfAssuranceDto.LEVEL_2;
            case LEVEL_3:
                return LevelOfAssuranceDto.LEVEL_3;
            case LEVEL_4:
                return LevelOfAssuranceDto.LEVEL_4;
            default:
                throw new IllegalArgumentException("Level of Assurance: '" + authnContext + "' is not a legal value in this context");
        }
    }
}
