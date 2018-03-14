package uk.gov.ida.matchingserviceadapter.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.controllogic.UnknownUserAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Function;

@Path(Urls.MatchingServiceAdapterUrls.UNKNOWN_USER_ATTRIBUTE_QUERY_PATH)
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public class UnknownUserAttributeQueryResource {

    private static final Logger LOG = LoggerFactory.getLogger(UnknownUserAttributeQueryResource.class);

    private final DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;
    private final Function<OutboundResponseFromUnknownUserCreationService, Element> responseElementTransformer;
    private final SoapMessageManager soapMessageManager;
    private final UnknownUserAttributeQueryHandler attributeQueryHandler;

    @Inject
    public UnknownUserAttributeQueryResource(
            DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper,
            Function<OutboundResponseFromUnknownUserCreationService, Element> responseElementTransformer,
            SoapMessageManager soapMessageManager,
            UnknownUserAttributeQueryHandler attributeQueryHandler) {
        this.documentToInboundMatchingServiceRequestMapper = documentToInboundMatchingServiceRequestMapper;
        this.responseElementTransformer = responseElementTransformer;
        this.soapMessageManager = soapMessageManager;
        this.attributeQueryHandler = attributeQueryHandler;
    }

    @POST
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveUnknownUserRequest(Document attributeQueryDocument) {

        LOG.debug("AttributeQuery POSTED: {}", attributeQueryDocument);

        InboundVerifyMatchingServiceRequest hubMatchingServiceRequest = documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(attributeQueryDocument);

        OutboundResponseFromUnknownUserCreationService samlResponse = getOutboundResponseFromMatchingService(hubMatchingServiceRequest);
        Element output = responseElementTransformer.apply(samlResponse);
        return Response.ok()
                .entity(soapMessageManager.wrapWithSoapEnvelope(output))
                .build();
    }

    private OutboundResponseFromUnknownUserCreationService getOutboundResponseFromMatchingService(InboundVerifyMatchingServiceRequest hubMatchingServiceRequest) {
        OutboundResponseFromUnknownUserCreationService response;
        try {
            response = attributeQueryHandler.handle(hubMatchingServiceRequest);
        } catch (WebApplicationException e) {
            throw new SamlOverSoapException("The matching service returned a http error.", e, hubMatchingServiceRequest.getId());
        }
        return response;
    }
}
