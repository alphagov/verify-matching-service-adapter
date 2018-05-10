package uk.gov.ida.matchingserviceadapter.mappers;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import static java.text.MessageFormat.format;
import static uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto.LEVEL_1;
import static uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto.LEVEL_2;
import static uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto.LEVEL_3;
import static uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto.LEVEL_4;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_HIGH;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_LOW;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL;
import static uk.gov.ida.saml.core.transformers.AuthnContextFactory.SAML_AUTHN_CONTEXT_IS_NOT_A_RECOGNISED_VALUE;

public class AuthnContextToLevelOfAssuranceDtoMapper {

    private AuthnContextToLevelOfAssuranceDtoMapper() {}

    public static LevelOfAssuranceDto map(AuthnContext authnContext) {
        switch (authnContext){
            case LEVEL_1:
                return LEVEL_1;
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

    public static AuthnContext map(LevelOfAssuranceDto levelOfAssurance) {
        switch (levelOfAssurance) {
            case LEVEL_1:
                return AuthnContext.LEVEL_1;
            case LEVEL_2:
                return AuthnContext.LEVEL_2;
            case LEVEL_3:
                return AuthnContext.LEVEL_3;
            case LEVEL_4:
                return AuthnContext.LEVEL_4;
            default:
                throw new IllegalStateException(format(SAML_AUTHN_CONTEXT_IS_NOT_A_RECOGNISED_VALUE, levelOfAssurance));
        }
    }
}
