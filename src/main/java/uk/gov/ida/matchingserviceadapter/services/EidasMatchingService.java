package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.TranslatedAttributeQueryRequest;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.AttributeQueryValidationException;
import uk.gov.ida.matchingserviceadapter.factories.EidasAttributeQueryValidatorFactory;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import javax.inject.Inject;

import java.util.List;

import static uk.gov.ida.matchingserviceadapter.mappers.AuthnContextToLevelOfAssuranceDtoMapper.map;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasMatchingService implements MatchingService {

    private final EidasAttributeQueryValidatorFactory attributeQueryValidatorFactory;
    private final EidasMatchingRequestToMSRequestTransformer transformer;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper;
    private final HubAssertionExtractor hubAssertionExtractor;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    @Inject
    public EidasMatchingService(EidasAttributeQueryValidatorFactory attributeQueryValidatorFactory,
                                EidasMatchingRequestToMSRequestTransformer transformer,
                                HubAssertionExtractor hubAssertionExtractor,
                                MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper) {
        this.attributeQueryValidatorFactory = attributeQueryValidatorFactory;
        this.transformer = transformer;
        this.hubAssertionExtractor = hubAssertionExtractor;
        this.responseMapper = responseMapper;
    }

    @Override
    public TranslatedAttributeQueryRequest translate(MatchingServiceRequestContext request) {
        Assertion countryAssertion = hubAssertionExtractor.getNonHubAssertions(request.getAssertions()).get(0);
        List<Attribute> extractedUserAccountCreationAttributes =
                userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(
                        request.getUserCreationAttributes(),
                        matchingDataset.orElse(null), attributeQuery.getCycle3AttributeAssertion().orElse(null)
                );

        return new TranslatedAttributeQueryRequest(extractMatchingDataset(request, countryAssertion),
                request.getAttributeQuery().getID(),
                request.getAttributeQuery().getSubject().getNameID().getNameQualifier(),
                request.getAttributeQuery().getSubject().getNameID().getSPNameQualifier(),
                userAccountCreationAttributes, false
        );
    }

    @Override
    public MatchingServiceResponse createOutboundResponse(MatchingServiceRequestContext requestContext, TranslatedAttributeQueryRequest request, MatchingServiceResponseDto response) {
        return new VerifyMatchingServiceResponse(
                responseMapper.map(
                        response,
                        request.getMatchingServiceRequestDto().getHashedPid(),
                        request.getIssuer(),
                        request.getAssertionConsumerServiceUrl(),
                        map(request.getMatchingServiceRequestDto().getLevelOfAssurance()),
                        request.getAuthnRequestIssuerId()
                )
        );
    }

    private UniversalMatchingServiceRequestDto extractMatchingDataset(MatchingServiceRequestContext request, Assertion countryAssertion) {
        String countryEntityId = countryAssertion.getIssuer().getValue();
        Validator<AttributeQuery> validator = attributeQueryValidatorFactory.build(countryEntityId);
        Messages validationMessages = validator.validate(request.getAttributeQuery(), messages());
        if (validationMessages.hasErrors()) {
            throw new AttributeQueryValidationException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        return transformer.apply(request);
    }
}
