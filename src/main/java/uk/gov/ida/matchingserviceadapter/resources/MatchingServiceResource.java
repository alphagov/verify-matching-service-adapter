package uk.gov.ida.matchingserviceadapter.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
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
    private final MatchingServiceResponseRenderer<MatchingServiceResponse> responseRenderer;

    @Inject
    public MatchingServiceResource(
        MatchingService matchingService,
        MatchingServiceResponseRenderer<MatchingServiceResponse> responseRenderer) {
        this.matchingService = matchingService;
        this.responseRenderer = responseRenderer;
    }

    @POST
    @Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH)
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveSoapRequest(Document attributeQueryDocument) {
        LOG.debug("AttributeQuery POSTED: {}", attributeQueryDocument);

        MatchingServiceResponse matchingServiceResponse = matchingService.handle(new MatchingServiceRequestContext(attributeQueryDocument));
        return responseRenderer.render(matchingServiceResponse);
    }
}
