package uk.gov.ida.matchingserviceadapter.resources;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

import javax.ws.rs.core.Response;
import java.util.function.Function;

public class VerifyMatchingServiceRestResponseGenerator implements MatchingServiceRestResponseGenerator<VerifyMatchingServiceResponse> {
    private SoapMessageManager soapMessageManager;
    private Function<OutboundResponseFromMatchingService, Element> responseElementTransformer;

    public VerifyMatchingServiceRestResponseGenerator(
        SoapMessageManager soapMessageManager,
        Function<OutboundResponseFromMatchingService, Element> responseElementTransformer

        ) {
        this.soapMessageManager = soapMessageManager;
        this.responseElementTransformer = responseElementTransformer;
    }

    @Override
    public Response generateResponse(VerifyMatchingServiceResponse verifyMatchingServiceResponse) {
        return Response.ok()
                .entity(soapMessageManager.wrapWithSoapEnvelope(responseElementTransformer.apply(verifyMatchingServiceResponse.getOutboundResponseFromMatchingService())))
                .build();
    }
}
