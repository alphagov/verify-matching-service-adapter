package uk.gov.ida.matchingserviceadapter.services;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VerifyMatchingServiceTest {

    /*@Mock
    private DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper;
    @Mock
    private MatchingServiceProxy matchingServiceProxy;
    @Mock
    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper requestMapper;
    @Mock
    private Document attributeQueryDocument;
    @Mock
    private MatchingServiceRequestContext requestContext;
    @Mock
    private InboundVerifyMatchingServiceRequest inboundRequest;
    @Mock
    private OutboundResponseFromMatchingService outboundResponse;

    private VerifyMatchingService service;

    @Before
    public void setup() {
        service = new VerifyMatchingService(documentToInboundMatchingServiceRequestMapper, requestMapper, );
        when(requestContext.getAttributeQueryDocument()).thenReturn(attributeQueryDocument);
        when(requestMapper.getInboundMatchingServiceRequest(attributeQueryDocument)).thenReturn(inboundRequest);
        when(attributeQueryHandler.handle(inboundRequest)).thenReturn(outboundResponse);
    }

    @Test
    public void shouldReturnCorrectResponse() {
        VerifyMatchingServiceResponse response = (VerifyMatchingServiceResponse)service.translate(requestContext);

        assertThat(response.getOutboundResponseFromMatchingService()).isEqualTo(outboundResponse);
    }

    @Test
    public void shouldReturnCorrectResponse() {
        VerifyMatchingServiceResponse response = (VerifyMatchingServiceResponse)service.translate(requestContext);

        assertThat(response.getOutboundResponseFromMatchingService()).isEqualTo(outboundResponse);
    }*/

}