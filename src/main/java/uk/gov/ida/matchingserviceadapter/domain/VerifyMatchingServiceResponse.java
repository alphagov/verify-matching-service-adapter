package uk.gov.ida.matchingserviceadapter.domain;


import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

public class VerifyMatchingServiceResponse implements MatchingServiceResponse {
    private OutboundResponseFromMatchingService outboundResponseFromMatchingService;

    public VerifyMatchingServiceResponse(OutboundResponseFromMatchingService outboundResponseFromMatchingService) {
        this.outboundResponseFromMatchingService = outboundResponseFromMatchingService;
    }

    public OutboundResponseFromMatchingService getOutboundResponseFromMatchingService() {
        return outboundResponseFromMatchingService;
    }
}
