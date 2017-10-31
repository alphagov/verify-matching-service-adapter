package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.Cycle3DatasetDtoBuilder.aCycle3DatasetDto;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.saml.core.test.builders.Cycle3DatasetBuilder.aCycle3Dataset;
import static uk.gov.ida.saml.core.test.builders.HubAssertionBuilder.aHubAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

@RunWith(MockitoJUnitRunner.class)
public class InboundMatchingServiceRequestToMatchingServiceRequestDtoMapperTest {

    public static final String ISSUER_ID = "issuerId";

    @Mock
    MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;

    @Mock
    UserIdHashFactory hashFactory;

    @Mock
    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(hashFactory, matchingServiceAdapterConfiguration, matchingDatasetToMatchingDatasetDtoMapper);
    }

    @Test
    public void map_shouldMapTheFieldsCorrectly() throws Exception {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;
        IdentityProviderAuthnStatement idaAuthnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(levelOfAssurance)
                .build();
        InboundMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withMatchingDatasetAssertion(anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build())
                .withAuthnStatementAssertion(anIdentityProviderAssertion().withAuthnStatement(idaAuthnStatement).build())
                .build();

        MatchingServiceRequestDto requestDto = mapper.map(request);

        verify(matchingDatasetToMatchingDatasetDtoMapper).map(matchingDataset);
        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(false);
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(levelOfAssuranceDto);
    }

    @Test
    public void map_shouldUseTheHashedPid() throws Exception {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        InboundMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withMatchingDatasetAssertion(anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build())
                .build();

        String hashedPid = "a-hashed-pid";
        when(matchingServiceAdapterConfiguration.getEntityId()).thenReturn(ISSUER_ID);
        when(hashFactory.createHashedId(request.getMatchingDatasetAssertion().getIssuerId(),
                ISSUER_ID,
                request.getMatchingDatasetAssertion().getPersistentId().getNameId(), request.getAuthnStatementAssertion().getAuthnStatement()))
                .thenReturn(hashedPid);

        MatchingServiceRequestDto requestDto = mapper.map(request);

        assertThat(requestDto.getHashedPid()).isEqualTo(hashedPid);
    }

    @Test
    public void map_shouldAddCycle3DatasetWhenPresent() throws Exception {
        Cycle3Dataset cycle3Data = aCycle3Dataset().build();
        Cycle3DatasetDto cycle3DatasetDto = aCycle3DatasetDto().build();
        InboundMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withCycle3DataAssertion(aHubAssertion().withCycle3Data(cycle3Data).build())
                .build();

        MatchingServiceRequestDto requestDto = mapper.map(request);

        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(true);
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).isEqualTo(cycle3DatasetDto.getAttributes());
    }
}
