package uk.gov.ida.matchingserviceadapter.exceptions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SamlOverSoapExceptionMapperTest {

    @Mock
    private ExceptionResponseFactory exceptionResponseFactory;
    @Mock
    private Element expectedElement;
    @Mock
    private SoapMessageManager soapMessageManager;
    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Test
    public void toResponseShouldReturnValidSamlResponseInSoapEnvelope() throws Exception {
        String requestId = UUID.randomUUID().toString();
        String issuerId = "an-entity-id";
        String errorMessage = "some message";
        when(exceptionResponseFactory.createResponse(requestId, issuerId, errorMessage)).thenReturn(expectedElement);
        Document expectedDocument = mock(Document.class);
        when(soapMessageManager.wrapWithSoapEnvelope(expectedElement)).thenReturn(expectedDocument);
        when(configuration.getEntityId()).thenReturn(issuerId);
        SamlOverSoapExceptionMapper samlOverSoapExceptionMapper = new SamlOverSoapExceptionMapper(soapMessageManager, exceptionResponseFactory, configuration);


        Response response = samlOverSoapExceptionMapper.toResponse(new SamlOverSoapException(errorMessage, new RuntimeException(), requestId));

        Document document = (Document) response.getEntity();

        assertThat(document).isEqualTo(expectedDocument);
    }
}
