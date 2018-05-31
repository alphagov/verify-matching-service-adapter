package uk.gov.ida.matchingserviceadapter.exceptions;

import com.google.inject.Inject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.text.MessageFormat;
import java.util.UUID;

public class SamlOverSoapExceptionMapper implements ExceptionMapper<SamlOverSoapException> {

    private static final Logger LOG = LoggerFactory.getLogger(SamlOverSoapExceptionMapper.class);

    private final SoapMessageManager soapMessageManager;
    private final ExceptionResponseFactory exceptionResponseFactory;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;

    @Inject
    public SamlOverSoapExceptionMapper(SoapMessageManager soapMessageManager, ExceptionResponseFactory exceptionResponseFactory, MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        this.soapMessageManager = soapMessageManager;
        this.exceptionResponseFactory = exceptionResponseFactory;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
    }

    @Override
    public Response toResponse(SamlOverSoapException exception) {
        UUID eventId = UUID.randomUUID();
        LOG.error(MessageFormat.format("{0} - Exception while processing request.", eventId), exception);

        try {
            Document soapMessage = soapMessageManager.wrapWithSoapEnvelope(exceptionResponseFactory.createResponse(exception.getRequestId(), matchingServiceAdapterConfiguration.getEntityId(), exception.getMessage()));
            return Response.serverError().entity(soapMessage).type(MediaType.TEXT_XML_TYPE).build();
        } catch (MarshallingException | SignatureException e) {
            LOG.error("Failed to create error response.");
            throw new RuntimeException(e);
        }
    }

}
