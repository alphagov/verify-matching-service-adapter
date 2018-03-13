package uk.gov.ida.matchingserviceadapter.domain;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class VerifyMatchingServiceResponseTest {
    @Test
    public void shouldStoreOutboundResponseFromMSA() {
        OutboundResponseFromMatchingService outboundResponseFromMatchingService = mock(OutboundResponseFromMatchingService.class);

        VerifyMatchingServiceResponse response = new VerifyMatchingServiceResponse(outboundResponseFromMatchingService);

        assertThat(response.getOutboundResponseFromMatchingService(), sameInstance(outboundResponseFromMatchingService));
    }
}