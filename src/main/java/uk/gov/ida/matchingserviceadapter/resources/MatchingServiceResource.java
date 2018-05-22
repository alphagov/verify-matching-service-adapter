package uk.gov.ida.matchingserviceadapter.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.domain.EncryptedAssertionContainer;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.logging.MdcHelper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.services.AttributeQueryService;
import uk.gov.ida.matchingserviceadapter.services.MatchingResponseGenerator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_ROOT)
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public class MatchingServiceResource {

    private static final Logger LOG = LoggerFactory.getLogger(MatchingServiceResource.class);

    private final AttributeQueryService attributeQueryService;
    private final MatchingResponseGenerator responseGenerator;
    private final SoapMessageManager soapMessageManager;
    private final ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller;
    private final MatchingServiceDtoMapper matchingRequestDtoMapper;
    private final MatchingServiceProxy matchingServiceProxy;
    private final MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
    private final AssertionDecrypter assertionDecrypter;

    @Inject
    public MatchingServiceResource(
            AttributeQueryService attributeQueryService,
            MatchingResponseGenerator responseGenerator,
            SoapMessageManager soapMessageManager,
            ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller,
            MatchingServiceDtoMapper matchingRequestDtoMapper,
            MatchingServiceProxy matchingServiceProxy,
            MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper, AssertionDecrypter assertionDecrypter) {
        this.attributeQueryService = attributeQueryService;
        this.responseGenerator = responseGenerator;
        this.soapMessageManager = soapMessageManager;
        this.attributeQueryUnmarshaller = attributeQueryUnmarshaller;
        this.matchingRequestDtoMapper = matchingRequestDtoMapper;
        this.matchingServiceProxy = matchingServiceProxy;
        this.matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper = matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
        this.assertionDecrypter = assertionDecrypter;
    }

    @POST
    @Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH)
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveSoapRequest(Document attributeQueryDocument) {
        LOG.debug("AttributeQuery POSTED: {}", attributeQueryDocument);

        AttributeQuery attributeQuery = unwrapAttributeQuery(attributeQueryDocument);
        List<Assertion> assertions = assertionDecrypter.decryptAssertions(new EncryptedAssertionContainer(attributeQuery));

        // If no assertions are present, assume this is a Health Check
        if (assertions.isEmpty()) {
            return responseGenerator.generateHealthCheckResponse(attributeQuery.getID());
        }

        try {
            attributeQueryService.validate(attributeQuery);
            attributeQueryService.validateAssertions(attributeQuery.getID(), assertions);
        } catch (SamlResponseValidationException | SamlTransformationErrorException ex) {
            throw new SamlOverSoapException(ex.getMessage(), ex, attributeQuery.getID());
        }

        AssertionData assertionData = attributeQueryService.getAssertionData(assertions);

        MatchingServiceRequestDto matchingServiceRequest = matchingRequestDtoMapper.map(
                attributeQuery.getID(),
                attributeQueryService.hashPid(assertionData),
                assertionData);
        MatchingServiceResponseDto matchingServiceResponse = matchingServiceProxy.makeMatchingServiceRequest(matchingServiceRequest);
        OutboundResponseFromMatchingService outboundResponseFromMatchingService = matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.map(matchingServiceResponse,
                matchingServiceRequest.getHashedPid(),
                attributeQuery.getID(),
                attributeQuery.getSubject().getNameID().getNameQualifier(),
                assertionData.getLevelOfAssurance(),
                attributeQuery.getSubject().getNameID().getSPNameQualifier());
        return responseGenerator.generateResponse(outboundResponseFromMatchingService);
    }

    private AttributeQuery unwrapAttributeQuery(Document attributeQueryDocument) {
        Element unwrappedMessage = soapMessageManager.unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery);
        AttributeQuery attributeQuery = attributeQueryUnmarshaller.apply(unwrappedMessage);
        MdcHelper.addContextToMdc(attributeQuery);
        return attributeQuery;
    }
}
