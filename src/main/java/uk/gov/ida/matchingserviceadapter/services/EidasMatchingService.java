package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.AttributeQueryValidationException;
import uk.gov.ida.matchingserviceadapter.factories.EidasAttributeQueryValidatorFactory;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import javax.inject.Inject;

import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasMatchingService implements MatchingService {

    private final EidasAttributeQueryValidatorFactory attributeQueryValidatorFactory;
    private final EidasMatchingRequestToMSRequestTransformer transformer;
    private final MatchingServiceProxy matchingServiceClient;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper;
    private final HubAssertionExtractor hubAssertionExtractor;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    @Inject
    public EidasMatchingService(EidasAttributeQueryValidatorFactory attributeQueryValidatorFactory,
                                EidasMatchingRequestToMSRequestTransformer transformer,
                                MatchingServiceProxy matchingServiceClient,
                                HubAssertionExtractor hubAssertionExtractor,
                                MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper) {
        this.attributeQueryValidatorFactory = attributeQueryValidatorFactory;
        this.transformer = transformer;
        this.matchingServiceClient = matchingServiceClient;
        this.hubAssertionExtractor = hubAssertionExtractor;
        this.responseMapper = responseMapper;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        Assertion countryAssertion = hubAssertionExtractor.getNonHubAssertions(request.getAssertions()).get(0);
        String countryEntityId = countryAssertion.getIssuer().getValue();
        Validator<AttributeQuery> validator = attributeQueryValidatorFactory.build(countryEntityId);
        Messages validationMessages = validator.validate(request.getAttributeQuery(), messages());
        if (validationMessages.hasErrors()) {
            throw new AttributeQueryValidationException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        UniversalMatchingServiceRequestDto universalMatchingServiceRequestDto = transformer.apply(request);
        MatchingServiceResponseDto responseFromMatchingService = matchingServiceClient.makeMatchingServiceRequest(universalMatchingServiceRequestDto);

        return new VerifyMatchingServiceResponse(
            responseMapper.map(
                responseFromMatchingService,
                universalMatchingServiceRequestDto.getHashedPid(),
                request.getAttributeQuery().getID(),
                request.getAttributeQuery().getSubject().getNameID().getNameQualifier(),
                authnContextFactory.mapFromEidasToLoA(countryAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()),
                request.getAttributeQuery().getSubject().getNameID().getSPNameQualifier()
            )
        );
    }
}
