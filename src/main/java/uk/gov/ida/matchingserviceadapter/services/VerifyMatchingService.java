package uk.gov.ida.matchingserviceadapter.services;

import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

import javax.ws.rs.WebApplicationException;

public class VerifyMatchingService implements MatchingService {
    private MatchingServiceAttributeQueryHandler attributeQueryHandler;
    private DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;

    public VerifyMatchingService(
        MatchingServiceAttributeQueryHandler attributeQueryHandler,
        DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper
    ) {
        this.attributeQueryHandler = attributeQueryHandler;
        this.documentToInboundMatchingServiceRequestMapper = documentToInboundMatchingServiceRequestMapper;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {

        InboundVerifyMatchingServiceRequest hubMatchingServiceRequest = documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(request.getAttributeQueryDocument());

        OutboundResponseFromMatchingService samlResponse = getOutboundResponseFromMatchingService(hubMatchingServiceRequest);
        return new VerifyMatchingServiceResponse(samlResponse);
    }

    private OutboundResponseFromMatchingService getOutboundResponseFromMatchingService(InboundVerifyMatchingServiceRequest hubMatchingServiceRequest) {
        OutboundResponseFromMatchingService response;
        try {
            response = attributeQueryHandler.handle(hubMatchingServiceRequest);
        } catch (WebApplicationException e) {
            throw new SamlOverSoapException("The matching service returned a http error.", e, hubMatchingServiceRequest.getId());
        }
        return response;
    }

}
