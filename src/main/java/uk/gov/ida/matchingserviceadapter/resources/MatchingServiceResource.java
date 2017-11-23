package uk.gov.ida.matchingserviceadapter.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Function;

@Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_ROOT)
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public class MatchingServiceResource {

    private static final Logger LOG = LoggerFactory.getLogger(MatchingServiceResource.class);

    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final SoapMessageManager soapMessageManager;
    private final Function<OutboundResponseFromMatchingService, Element> responseElementTransformer;
    private final Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    private final MatchingServiceAttributeQueryHandler attributeQueryHandler;
    private final DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;
    private final ManifestReader manifestReader;

    @Inject
    public MatchingServiceResource(
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            SoapMessageManager soapMessageManager,
            Function<OutboundResponseFromMatchingService, Element> responseElementTransformer,
            Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer,
            MatchingServiceAttributeQueryHandler attributeQueryHandler,
            DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper,
            ManifestReader manifestReader) {

        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.soapMessageManager = soapMessageManager;
        this.responseElementTransformer = responseElementTransformer;
        this.healthCheckResponseTransformer = healthCheckResponseTransformer;
        this.attributeQueryHandler = attributeQueryHandler;
        this.documentToInboundMatchingServiceRequestMapper = documentToInboundMatchingServiceRequestMapper;
        this.manifestReader = manifestReader;
    }

    @POST
    @Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH)
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveSoapRequest(Document attributeQueryDocument) {

        LOG.debug("AttributeQuery POSTED: {}", attributeQueryDocument);

        InboundMatchingServiceRequest hubMatchingServiceRequest = documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(attributeQueryDocument);

        Response.ResponseBuilder httpResponse = Response.ok();
        if (isHealthCheck(hubMatchingServiceRequest)) {
            final String requestId = hubMatchingServiceRequest.getId();

            LOG.info("Responding to health check with id '{}'.", requestId);
            String version = manifestReader.getManifest().getValue("Version-Number");
            return httpResponse
                    .header("ida-msa-version", version)
                    .entity(soapMessageManager.wrapWithSoapEnvelope(healthCheckResponseTransformer.apply(
                            new HealthCheckResponseFromMatchingService(matchingServiceAdapterConfiguration.getEntityId(), requestId, version))))
                    .build();

        } else {
            OutboundResponseFromMatchingService samlResponse = getOutboundResponseFromMatchingService(hubMatchingServiceRequest);
            return httpResponse
                    .entity(soapMessageManager.wrapWithSoapEnvelope(responseElementTransformer.apply(samlResponse)))
                    .build();

        }
    }

    private boolean isHealthCheck(InboundMatchingServiceRequest hubMatchingServiceRequest) {
        return hubMatchingServiceRequest.getAuthnStatementAssertion() == null
                && hubMatchingServiceRequest.getMatchingDatasetAssertion() == null;
    }

    private OutboundResponseFromMatchingService getOutboundResponseFromMatchingService(InboundMatchingServiceRequest hubMatchingServiceRequest) {
        OutboundResponseFromMatchingService response;
        try {
            response = attributeQueryHandler.handle(hubMatchingServiceRequest);
        } catch (WebApplicationException e) {
            throw new SamlOverSoapException("The matching service returned a http error.", e, hubMatchingServiceRequest.getId());
        }
        return response;
    }

}
