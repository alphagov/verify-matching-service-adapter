package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.base.Optional;
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
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.UnknownUserCreationIdaStatus;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.PersistentIdBuilder;
import uk.gov.ida.saml.core.test.builders.SimpleMdsValueBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.FAILURE;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.SUCCESS;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

@RunWith(OpenSAMLMockitoRunner.class)
public class UnknownUserAttributeQueryHandlerTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private MatchingServiceAssertionFactory assertionFactory;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Mock
    private MatchingServiceProxy matchingServiceProxy;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    private UnknownUserAttributeQueryHandler unknownUserAttributeQueryHandler;

    private String hashedPid = "hashedPid";
    private String issuerId = "some-idp";
    private String nameId = "nameId";
    private AuthnContext authnContext = AuthnContext.LEVEL_2;
    private LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;

    @Before
    public void setup() {
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.days(2));
        when(userIdHashFactory.hashId(issuerId, nameId, Optional.of(authnContext))).thenReturn(hashedPid);

        unknownUserAttributeQueryHandler = new UnknownUserAttributeQueryHandler(
            userIdHashFactory, configuration,
            assertionFactory,
            assertionLifetimeConfiguration,
            matchingServiceProxy,
            new UserAccountCreationAttributeExtractor());
    }

    @Test
    public void shouldReturnSuccessResponseWhenMatchingServiceReturnsSuccess() {
        when(matchingServiceProxy.makeUnknownUserCreationRequest(new UnknownUserCreationRequestDto(hashedPid, levelOfAssuranceDto)))
            .thenReturn(new UnknownUserCreationResponseDto(SUCCESS));

        OutboundResponseFromUnknownUserCreationService response = unknownUserAttributeQueryHandler.handle(buildInboundVerifyMatchingServiceRequest());

        assertThat(response.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.Success);
    }

    @Test
    public void shouldReturnFailureResponseWhenMatchingServiceReturnsFailure() {
        when(matchingServiceProxy.makeUnknownUserCreationRequest(new UnknownUserCreationRequestDto(hashedPid, levelOfAssuranceDto)))
            .thenReturn(new UnknownUserCreationResponseDto(FAILURE));

        OutboundResponseFromUnknownUserCreationService handle = unknownUserAttributeQueryHandler.handle(buildInboundVerifyMatchingServiceRequest());

        assertThat(handle.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.CreateFailure);
    }

    private InboundVerifyMatchingServiceRequest buildInboundVerifyMatchingServiceRequest(){
        return anInboundMatchingServiceRequest()
                .withAuthnStatementAssertion(
                        anIdentityProviderAssertion()
                                .withAuthnStatement(anIdentityProviderAuthnStatement().withAuthnContext(authnContext).build())
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
                                .withIssuerId(issuerId)
                                .withPersistentId(
                                        PersistentIdBuilder.aPersistentId()
                                                .withNameId(nameId)
                                                .build())
                                .build()
                )
                .buildForVerify();
    }
}
