package uk.gov.ida.matchingserviceadapter.domain;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

import java.util.function.Function;

public class EidasToVerifyLoaTransformer implements Function<EidasLoa, LevelOfAssuranceDto> {

    @Override
    public LevelOfAssuranceDto apply(EidasLoa eidasLoa) {
        switch( eidasLoa ) {
            case LOW:
                return LevelOfAssuranceDto.LEVEL_1;
            case SUBSTANTIAL:
                return LevelOfAssuranceDto.LEVEL_2;
            case HIGH:
                return LevelOfAssuranceDto.LEVEL_3;
            default:
                throw new IllegalArgumentException(String.format("Unable to convert Eidas level of assurance '%s' to Verify loa", eidasLoa));
        }
    }
}
