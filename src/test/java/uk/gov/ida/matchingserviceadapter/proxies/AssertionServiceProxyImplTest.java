package uk.gov.ida.matchingserviceadapter.proxies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceRequestDtoBuilder.aMatchingServiceRequestDto;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceResponseDtoBuilder.aMatchingServiceResponseDto;
import static uk.gov.ida.matchingserviceadapter.builders.UnknownUserCreationRequestDtoBuilder.anUnknnownUserCreationRequestDto;
import static uk.gov.ida.matchingserviceadapter.builders.UnknownUserCreationResponseDtoBuilder.anUnknownUserCreationResponseDto;

@RunWith(MockitoJUnitRunner.class)
public class AssertionServiceProxyImplTest {

    @Mock
    private JsonClient client;

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    private MatchingServiceProxyImpl proxy;

    @Before
    public void setUp() {
        proxy = new MatchingServiceProxyImpl(client, configuration);
    }

    @Test
    public void sendRequestToMatchingService_shouldReturnAMatchingServiceResponseDto() throws Exception {
        VerifyMatchingServiceRequestDto verifyMatchingServiceRequestDto = aMatchingServiceRequestDto().buildVerifyMatchingServiceRequestDto();
        URI localMatchingServiceUri = URI.create("http://a-uri");
        when(configuration.getLocalMatchingServiceMatchUrl()).thenReturn(localMatchingServiceUri);

        MatchingServiceResponseDto expectedResponse = aMatchingServiceResponseDto().build();
        when(client.post(
                verifyMatchingServiceRequestDto,
                localMatchingServiceUri,
                MatchingServiceResponseDto.class)).thenReturn(expectedResponse);

        MatchingServiceResponseDto actualResponse = proxy.makeMatchingServiceRequest(verifyMatchingServiceRequestDto);

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse);
    }

    @Test
    public void makeUnknownUserCreationRequest_shouldReturnAnAppropriateDto() throws Exception {
        UnknownUserCreationRequestDto request = anUnknnownUserCreationRequestDto().build();
        UnknownUserCreationResponseDto expectedResponse = anUnknownUserCreationResponseDto().build();

        URI uri = URI.create("http://b-uri");
        when(configuration.getLocalMatchingServiceAccountCreationUrl()).thenReturn(uri);

        when(client.post(
                request,
                uri,
                UnknownUserCreationResponseDto.class)).thenReturn(expectedResponse);

        UnknownUserCreationResponseDto response = proxy.makeUnknownUserCreationRequest(request);

        assertThat(response).isEqualToComparingFieldByField(expectedResponse);
    }
}
