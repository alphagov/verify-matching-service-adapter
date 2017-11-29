package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

public class MatchingServiceAttributeQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MatchingServiceAttributeQueryHandler.class);

    private final MatchingServiceProxy matchingServiceProxy;
    private final InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper queryDtoMapper;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper dtoResponseMapper;

    @Inject
    public MatchingServiceAttributeQueryHandler(
        MatchingServiceProxy matchingServiceProxy,
        InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper queryDtoMapper,
        MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper dtoResponseMapper) {
        this.matchingServiceProxy = matchingServiceProxy;
        this.queryDtoMapper = queryDtoMapper;
        this.dtoResponseMapper = dtoResponseMapper;
    }

    public OutboundResponseFromMatchingService handle(InboundMatchingServiceRequest attributeQuery) {
        MatchingServiceRequestDto matchingServiceAttributeQuery = queryDtoMapper.map(attributeQuery);
        MatchingServiceResponseDto matchingServiceResponseDto = matchingServiceProxy.makeMatchingServiceRequest(matchingServiceAttributeQuery);
        LOG.info("Result from matching service for id " + attributeQuery.getId() + " is " + matchingServiceResponseDto.getResult());
        return dtoResponseMapper.map(matchingServiceResponseDto, matchingServiceAttributeQuery.getHashedPid(), attributeQuery.getId(), attributeQuery.getAssertionConsumerServiceUrl(), attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(), attributeQuery.getAuthnRequestIssuerId());
    }
}
