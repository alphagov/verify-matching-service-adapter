package uk.gov.ida.matchingserviceadapter.services;

import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.Validator;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.AttributeQueryValidationException;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;

import javax.inject.Inject;
import java.util.function.Function;

import static org.beanplanet.messages.domain.MessagesImpl.messages;

public class EidasMatchingService implements MatchingService {

    private final Validator<AttributeQuery> validator;
    private final Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer;
    private final MatchingServiceProxy matchingServiceClient;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    @Inject
    public EidasMatchingService(Validator<AttributeQuery> validator,
                                Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer,
                                MatchingServiceProxy matchingServiceClient,
                                MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper) {
        this.validator = validator;
        this.transformer = transformer;
        this.matchingServiceClient = matchingServiceClient;
        this.responseMapper = responseMapper;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        Messages validationMessages = validator.validate(request.getAttributeQuery(), messages());
        if (validationMessages.hasErrors()) {
            throw new AttributeQueryValidationException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        MatchingServiceRequestDto matchingServiceRequestDto = transformer.apply(request);
        MatchingServiceResponseDto responseFromMatchingService = matchingServiceClient.makeMatchingServiceRequest(matchingServiceRequestDto);

        return new VerifyMatchingServiceResponse(
            responseMapper.map(
                responseFromMatchingService,
                matchingServiceRequestDto.getHashedPid(),
                request.getAttributeQuery().getID(),
                request.getAttributeQuery().getSubject().getNameID().getNameQualifier(),
                authnContextFactory.mapFromEidasToLoA(request.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()),
                request.getAttributeQuery().getSubject().getNameID().getSPNameQualifier()
            )
        );
    }
}
