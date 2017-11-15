package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.proxies.AdapterToMatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.UnknownUserCreationIdaStatus;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.SimpleMdsValueBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.FAILURE;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.SUCCESS;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;

@RunWith(OpenSAMLMockitoRunner.class)
public class UnknownUserAttributeQueryHandlerTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private MatchingServiceAssertionFactory assertionFactory;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Mock
    private AdapterToMatchingServiceProxy adapterToMatchingServiceProxy;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    private UnknownUserAttributeQueryHandler unknownUserAttributeQueryHandler;

    private String hashedPid = "hashedPid";

    @Before
    public void setup() {
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.days(2));
        unknownUserAttributeQueryHandler = new UnknownUserAttributeQueryHandler(
            userIdHashFactory, configuration,
                assertionFactory,
                assertionLifetimeConfiguration,
                adapterToMatchingServiceProxy,
                new UserAccountCreationAttributeExtractor());
        when(userIdHashFactory.hashId(anyString(), anyString(), anyObject())).thenReturn(hashedPid);
    }

    @Test
    public void shouldReturnSuccessResponseWhenMatchingServiceReturnsSuccess() {
        InboundMatchingServiceRequest inboundMatchingServiceRequest = anInboundMatchingServiceRequest()
                .withAuthnStatementAssertion(
                        anIdentityProviderAssertion()
                                .withAuthnStatement(anIdentityProviderAuthnStatement().withAuthnContext(AuthnContext.LEVEL_1).build())
                                .build()
                )
                .withUserCreationAttributes(ImmutableList.of(FIRST_NAME))
                .withMatchingDatasetAssertion(
                        anIdentityProviderAssertion()
                                .withMatchingDataset(
                                        aMatchingDataset().addFirstname(
                                                SimpleMdsValueBuilder.<String>aSimpleMdsValue()
                                                        .withValue("name")
                                                        .withFrom(null)
                                                        .withTo(null)
                                                        .build())
                                        .build())
                                .build()
                )
                .build();
        when(adapterToMatchingServiceProxy.makeUnknownUserCreationRequest(new UnknownUserCreationRequestDto(hashedPid, LevelOfAssuranceDto.LEVEL_1)))
                .thenReturn(new UnknownUserCreationResponseDto(SUCCESS));

        OutboundResponseFromUnknownUserCreationService response = unknownUserAttributeQueryHandler.handle(inboundMatchingServiceRequest);
        assertThat(response.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.Success);
    }

    @Test
    public void shouldReturnFailureResponseWhenMatchingServiceReturnsFailure() {
        InboundMatchingServiceRequest inboundMatchingServiceRequest = anInboundMatchingServiceRequest()
                .withAuthnStatementAssertion(
                        anIdentityProviderAssertion()
                                .withAuthnStatement(anIdentityProviderAuthnStatement().withAuthnContext(AuthnContext.LEVEL_2).build())
                                .build()
                )
                .build();
        when(adapterToMatchingServiceProxy.makeUnknownUserCreationRequest(new UnknownUserCreationRequestDto(hashedPid, LevelOfAssuranceDto.LEVEL_2)))
                .thenReturn(new UnknownUserCreationResponseDto(FAILURE));

        OutboundResponseFromUnknownUserCreationService handle = unknownUserAttributeQueryHandler.handle(inboundMatchingServiceRequest);
        assertThat(handle.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.CreateFailure);
    }
}
