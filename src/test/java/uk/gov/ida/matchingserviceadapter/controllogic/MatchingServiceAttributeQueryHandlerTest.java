package uk.gov.ida.matchingserviceadapter.controllogic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.AuthnContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceRequestDtoBuilder.aMatchingServiceRequestDto;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceResponseDtoBuilder.aMatchingServiceResponseDto;
import static uk.gov.ida.matchingserviceadapter.builders.OutboundResponseFromMatchingServiceBuilder.aResponse;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceAttributeQueryHandlerTest {

    @Mock
    MatchingServiceProxy matchingServiceProxy;

    @Mock
    InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper queryDtoMapper;

    @Mock
    MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper dtoResponseMapper;


    @Test
    public void handle_shouldPassMapperOutputToProxy() throws Exception {
        InboundVerifyMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest().build();
        AuthnContext authnContext = attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext();
        String hashedPid = "pid";
        VerifyMatchingServiceRequestDto matchingServiceAttributeQuery = aMatchingServiceRequestDto().withHashedPid(hashedPid).buildVerifyMatchingServiceRequestDto();
        MatchingServiceResponseDto responseDto = aMatchingServiceResponseDto().build();
        OutboundResponseFromMatchingService idaResponse = aResponse().build();

        MatchingServiceAttributeQueryHandler handler = new MatchingServiceAttributeQueryHandler(matchingServiceProxy, queryDtoMapper, dtoResponseMapper);
        when(queryDtoMapper.map(attributeQuery)).thenReturn(matchingServiceAttributeQuery);
        when(matchingServiceProxy.makeMatchingServiceRequest(matchingServiceAttributeQuery)).thenReturn(responseDto);
        when(dtoResponseMapper.map(responseDto, hashedPid, attributeQuery.getId(), attributeQuery.getAssertionConsumerServiceUrl(), authnContext, attributeQuery.getAuthnRequestIssuerId()))
                .thenReturn(idaResponse);
        OutboundResponseFromMatchingService result = handler.handle(attributeQuery);

        assertThat(result).isEqualTo(idaResponse);
    }
}
