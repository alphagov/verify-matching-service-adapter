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
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.logging.MdcHelper;
import uk.gov.ida.matchingserviceadapter.mappers.AuthnContextToLevelOfAssuranceDtoMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.services.AttributeQueryService;
import uk.gov.ida.matchingserviceadapter.services.UnknownUserResponseGenerator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

@Path(Urls.MatchingServiceAdapterUrls.UNKNOWN_USER_ATTRIBUTE_QUERY_PATH)
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public class UnknownUserAttributeQueryResource {

    private static final Logger LOG = LoggerFactory.getLogger(UnknownUserAttributeQueryResource.class);

    private final Function<OutboundResponseFromUnknownUserCreationService, Element> responseElementTransformer;
    private final SoapMessageManager soapMessageManager;
    private final UnknownUserResponseGenerator attributeQueryHandler;
    private final ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller;
    private final AttributeQueryService attributeQueryService;
    private final MatchingServiceProxy matchingServiceProxy;
    private final AssertionDecrypter assertionDecrypter;

    @Inject
    public UnknownUserAttributeQueryResource(
            Function<OutboundResponseFromUnknownUserCreationService, Element> responseElementTransformer,
            SoapMessageManager soapMessageManager,
            UnknownUserResponseGenerator attributeQueryHandler,
            ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller,
            AttributeQueryService attributeQueryService,
            MatchingServiceProxy matchingServiceProxy,
            AssertionDecrypter assertionDecrypter) {
        this.responseElementTransformer = responseElementTransformer;
        this.soapMessageManager = soapMessageManager;
        this.attributeQueryHandler = attributeQueryHandler;
        this.attributeQueryUnmarshaller = attributeQueryUnmarshaller;
        this.attributeQueryService = attributeQueryService;
        this.matchingServiceProxy = matchingServiceProxy;
        this.assertionDecrypter = assertionDecrypter;
    }

    @POST
    @Timed(name= Urls.SOAP_TIMED_GROUP)
    public Response receiveUnknownUserRequest(Document attributeQueryDocument) {
        AttributeQuery attributeQuery = unwrapAttributeQuery(attributeQueryDocument);
        List<Assertion> assertions = assertionDecrypter.decryptAssertions(new EncryptedAssertionContainer(attributeQuery));
        try {
            attributeQueryService.validate(attributeQuery);
            attributeQueryService.validateAssertions(attributeQuery.getID(), assertions);
        } catch (SamlResponseValidationException | SamlTransformationErrorException ex) {
            throw new SamlOverSoapException(ex.getMessage(), ex, attributeQuery.getID());
        }

        AssertionData assertionData = attributeQueryService.getAssertionData(assertions);

        UnknownUserCreationResponseDto unknownUserCreationResponseDto = matchingServiceProxy.makeUnknownUserCreationRequest(
                new UnknownUserCreationRequestDto(attributeQueryService.hashPid(assertionData),
                        AuthnContextToLevelOfAssuranceDtoMapper.map(assertionData.getLevelOfAssurance())));
        LOG.info(MessageFormat.format("Result from unknown attribute query request for id {0} is {1}", attributeQuery.getID(), unknownUserCreationResponseDto.getResult()));

        OutboundResponseFromUnknownUserCreationService outboundResponse = attributeQueryHandler.getMatchingServiceResponse(
                unknownUserCreationResponseDto,
                attributeQuery.getID(),
                attributeQueryService.hashPid(assertionData),
                attributeQuery.getSubject().getNameID().getNameQualifier(),
                attributeQuery.getSubject().getNameID().getSPNameQualifier(),
                assertionData,
                attributeQuery.getAttributes()
        );

        Element output = responseElementTransformer.apply(outboundResponse);
        return Response.ok()
                .entity(soapMessageManager.wrapWithSoapEnvelope(output))
                .build();
    }

    private AttributeQuery unwrapAttributeQuery(Document attributeQueryDocument) {
        Element unwrappedMessage = soapMessageManager.unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery);
        AttributeQuery attributeQuery = attributeQueryUnmarshaller.apply(unwrappedMessage);
        MdcHelper.addContextToMdc(attributeQuery);
        return attributeQuery;
    }
}
