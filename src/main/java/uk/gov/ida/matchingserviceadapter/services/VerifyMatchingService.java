package uk.gov.ida.matchingserviceadapter.services;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.TranslatedAttributeQueryRequest;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;

import static uk.gov.ida.matchingserviceadapter.mappers.AuthnContextToLevelOfAssuranceDtoMapper.map;

public class VerifyMatchingService implements MatchingService {
    private DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;
    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper inboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper dtoResponseMapper;

    public VerifyMatchingService(
            DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper,
            InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper inboundMatchingServiceRequestToMatchingServiceRequestDtoMapper,
            MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper dtoResponseMapper) {
        this.inboundMatchingServiceRequestToMatchingServiceRequestDtoMapper = inboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
        this.documentToInboundMatchingServiceRequestMapper = documentToInboundMatchingServiceRequestMapper;
        this.dtoResponseMapper = dtoResponseMapper;
    }

    @Override
    public TranslatedAttributeQueryRequest translate(MatchingServiceRequestContext request) {

        InboundVerifyMatchingServiceRequest hubMatchingServiceRequest = documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(request.getAttributeQueryDocument());
        return new TranslatedAttributeQueryRequest(inboundMatchingServiceRequestToMatchingServiceRequestDtoMapper.map(hubMatchingServiceRequest),
                hubMatchingServiceRequest.getIssuer(),
                hubMatchingServiceRequest.getAssertionConsumerServiceUrl(),
                hubMatchingServiceRequest.getAuthnRequestIssuerId(),
                userAccountCreationAttributes, false
        );
    }

    @Override
    public MatchingServiceResponse createOutboundResponse(MatchingServiceRequestContext requestContext, TranslatedAttributeQueryRequest request, MatchingServiceResponseDto response) {
        return new VerifyMatchingServiceResponse(dtoResponseMapper.map(
                response,
                request.getMatchingServiceRequestDto().getHashedPid(),
                request.getMatchingServiceRequestDto().getMatchId(),
                request.getAssertionConsumerServiceUrl(),
                map(request.getMatchingServiceRequestDto().getLevelOfAssurance()),
                request.getAuthnRequestIssuerId())
        );
    }
}
