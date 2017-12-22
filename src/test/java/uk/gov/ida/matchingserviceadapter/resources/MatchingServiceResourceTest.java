package uk.gov.ida.matchingserviceadapter.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;

import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceResourceTest {

    @Mock
    private MatchingService matchingService;
    @Mock
    private MatchingServiceRestResponseRenderer<MatchingServiceResponse> responseRenderer;
    @Mock
    private Document attributeQueryDocument;
    @Mock
    private MatchingServiceResponse matchingServiceResponse;

    private MatchingServiceResource matchingServiceResource;

    @Before
    public void setUp() {
        matchingServiceResource = new MatchingServiceResource(
            matchingService,
            responseRenderer);
    }

    @Test
    public void testMatching() throws ParserConfigurationException {
        when(matchingService.handle(any())).thenReturn(matchingServiceResponse);
        matchingServiceResource.receiveSoapRequest(attributeQueryDocument);

        ArgumentCaptor<MatchingServiceRequestContext> requestContextArgumentCaptor = ArgumentCaptor.forClass(MatchingServiceRequestContext.class);
        verify(matchingService).handle(requestContextArgumentCaptor.capture());
        verify(responseRenderer).render(matchingServiceResponse);

        assertThat(requestContextArgumentCaptor.getValue().getAttributeQueryDocument()).isSameAs(attributeQueryDocument);
    }
}