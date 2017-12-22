package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VerifyMatchingServiceTest {

    @Mock
    private MatchingServiceAttributeQueryHandler attributeQueryHandler;
    @Mock
    private DocumentToInboundMatchingServiceRequestMapper requestMapper;
    @Mock
    private Document attributeQueryDocument;
    @Mock
    private MatchingServiceRequestContext requestContext;
    @Mock
    private InboundMatchingServiceRequest inboundRequest;
    @Mock
    private OutboundResponseFromMatchingService outboundResponse;

    private VerifyMatchingService service;

    @Before
    public void setup() {
        service = new VerifyMatchingService(attributeQueryHandler, requestMapper);
        when(requestContext.getAttributeQueryDocument()).thenReturn(attributeQueryDocument);
        when(requestMapper.getInboundMatchingServiceRequest(attributeQueryDocument)).thenReturn(inboundRequest);
        when(attributeQueryHandler.handle(inboundRequest)).thenReturn(outboundResponse);
    }

    @Test
    public void shouldReturnCorrectResponse() {
        VerifyMatchingServiceResponse response = (VerifyMatchingServiceResponse)service.handle(requestContext);

        assertThat(response.getOutboundResponseFromMatchingService()).isEqualTo(outboundResponse);
    }

}