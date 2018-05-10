package uk.gov.ida.matchingserviceadapter.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.TranslatedAttributeQueryRequest;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_ROOT)
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public class MatchingServiceResource {

    private static final Logger LOG = LoggerFactory.getLogger(MatchingServiceResource.class);

    private final MatchingService matchingService;
    private final MatchingServiceProxy matchingServiceClient;
    private final MatchingServiceResponseGenerator<MatchingServiceResponse> responseGenerator;

    @Inject
    public MatchingServiceResource(
            MatchingService matchingService,
            MatchingServiceProxy matchingServiceClient,
            MatchingServiceResponseGenerator<MatchingServiceResponse> responseGenerator) {
        this.matchingService = matchingService;
        this.matchingServiceClient = matchingServiceClient;
        this.responseGenerator = responseGenerator;
    }

    @POST
    @Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH)
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveSoapRequest(Document attributeQueryDocument) {
        LOG.debug("AttributeQuery POSTED: {}", attributeQueryDocument);

        MatchingServiceRequestContext requestContext = new MatchingServiceRequestContext(attributeQueryDocument);
        TranslatedAttributeQueryRequest translatedAttributeQueryRequest = matchingService.translate(requestContext);
        MatchingServiceResponseDto responseFromMatchingService = null;
        if (!translatedAttributeQueryRequest.isHealthCheck()) {
            responseFromMatchingService = matchingServiceClient.makeMatchingServiceRequest(translatedAttributeQueryRequest.getMatchingServiceRequestDto());
        }
        MatchingServiceResponse matchingServiceResponse = matchingService.createOutboundResponse(requestContext, translatedAttributeQueryRequest, responseFromMatchingService);
        return responseGenerator.generateResponse(matchingServiceResponse);
    }
}
