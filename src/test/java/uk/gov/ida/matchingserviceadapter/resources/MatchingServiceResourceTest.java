package uk.gov.ida.matchingserviceadapter.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;
import uk.gov.ida.shared.utils.xml.XmlUtils;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.util.function.Function;
import java.util.jar.Attributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceResourceTest {

    @Mock
    private MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    @Mock
    private SoapMessageManager soapMessageManager;
    @Mock
    private Function<OutboundResponseFromMatchingService, Element> responseElementTransformer;
    @Mock
    private Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    @Mock
    private MatchingServiceAttributeQueryHandler attributeQueryHandler;
    @Mock
    private DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;
    @Mock
    private ManifestReader manifestReader;

    MatchingServiceResource matchingServiceResource;

    @Before
    public void setUp() {
        matchingServiceResource = new MatchingServiceResource(matchingServiceAdapterConfiguration, soapMessageManager, responseElementTransformer, healthCheckResponseTransformer, attributeQueryHandler, documentToInboundMatchingServiceRequestMapper, manifestReader);
    }

    @Test
    public void testMatching() throws ParserConfigurationException {
        final Document inDocument = XmlUtils.newDocumentBuilder().newDocument();
        final InboundMatchingServiceRequest inboundMatchingServiceRequest = InboundMatchingServiceRequestBuilder
                .anInboundMatchingServiceRequest()
                .build();
        when(documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(inDocument)).thenReturn(inboundMatchingServiceRequest);

        final Response response = matchingServiceResource.receiveSoapRequest(inDocument);

        verify(attributeQueryHandler, times(1)).handle(inboundMatchingServiceRequest);
        verify(soapMessageManager, times(1)).wrapWithSoapEnvelope(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }


    @Test
    public void testHealthcheck() throws ParserConfigurationException {
        final Document inDocument = XmlUtils.newDocumentBuilder().newDocument();
        final InboundMatchingServiceRequest inboundMatchingServiceRequest = InboundMatchingServiceRequestBuilder
                .anInboundMatchingServiceRequest()
                .withAuthnStatementAssertion(null)
                .withMatchingDatasetAssertion(null)
                .build();
        when(documentToInboundMatchingServiceRequestMapper.getInboundMatchingServiceRequest(inDocument)).thenReturn(inboundMatchingServiceRequest);
        when(manifestReader.getManifest()).thenReturn(new Attributes());

        final Response response = matchingServiceResource.receiveSoapRequest(inDocument);

        verify(soapMessageManager, times(1)).wrapWithSoapEnvelope(any());
        verify(healthCheckResponseTransformer, times(1)).apply(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}