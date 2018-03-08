package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
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

    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Mock
    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper mapper;

    @Before
    public void setUp() {
        mapper = new InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(userIdHashFactory, matchingDatasetToMatchingDatasetDtoMapper);
    }

    @Test
    public void map_shouldMapTheFieldsCorrectly() {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;
        IdentityProviderAuthnStatement idaAuthnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(levelOfAssurance)
                .build();
        InboundVerifyMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withMatchingDatasetAssertion(anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build())
                .withAuthnStatementAssertion(anIdentityProviderAssertion().withAuthnStatement(idaAuthnStatement).build())
                .build();

        VerifyMatchingServiceRequestDto requestDto = mapper.map(request);

        verify(matchingDatasetToMatchingDatasetDtoMapper).mapToVerifyMatchingDatasetDto(matchingDataset);
        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(false);
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(levelOfAssuranceDto);
    }

    @Test
    public void map_shouldUseTheHashedPid() {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        InboundVerifyMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withMatchingDatasetAssertion(anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build())
                .build();
        String hashedPid = "a-hashed-pid";
        Optional<AuthnContext> levelOfAssurance = request.getAuthnStatementAssertion().getAuthnStatement().transform(IdentityProviderAuthnStatement::getAuthnContext);
        when(userIdHashFactory.hashId(request.getMatchingDatasetAssertion().getIssuerId(), request.getMatchingDatasetAssertion().getPersistentId().getNameId(), levelOfAssurance)).thenReturn(hashedPid);

        VerifyMatchingServiceRequestDto requestDto = mapper.map(request);

        assertThat(requestDto.getHashedPid()).isEqualTo(hashedPid);
    }

    @Test
    public void map_shouldAddCycle3DatasetWhenPresent() {
        Cycle3Dataset cycle3Data = aCycle3Dataset().build();
        Cycle3DatasetDto cycle3DatasetDto = aCycle3DatasetDto().build();
        InboundVerifyMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withCycle3DataAssertion(aHubAssertion().withCycle3Data(cycle3Data).build())
                .build();

        VerifyMatchingServiceRequestDto requestDto = mapper.map(request);

        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(true);
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).isEqualTo(cycle3DatasetDto.getAttributes());
    }
}
