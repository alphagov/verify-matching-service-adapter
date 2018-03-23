package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundEidasMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.Cycle3DatasetDtoBuilder.aCycle3DatasetDto;
import static uk.gov.ida.matchingserviceadapter.builders.EidasMatchingDatasetBuilder.anEidasMatchingDataset;
import static uk.gov.ida.matchingserviceadapter.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.matchingserviceadapter.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetBuilder.aMatchingDataset;
import static uk.gov.ida.matchingserviceadapter.builders.ProxyNodeAssertionBuilder.anProxyNodeAssertion;
import static uk.gov.ida.saml.core.test.builders.Cycle3DatasetBuilder.aCycle3Dataset;
import static uk.gov.ida.saml.core.test.builders.HubAssertionBuilder.aHubAssertion;

@RunWith(MockitoJUnitRunner.class)
public class InboundMatchingServiceRequestToMatchingServiceRequestDtoMapperTest {

    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Mock
    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper verifyOnlyMapper;
    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper eidasEnabledMapper;

    @Before
    public void setUp() {
        eidasEnabledMapper = new InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(userIdHashFactory, matchingDatasetToMatchingDatasetDtoMapper, true);
        verifyOnlyMapper = new InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(userIdHashFactory, matchingDatasetToMatchingDatasetDtoMapper, false);
    }

    @Test
    public void map_verify_shouldMapTheFieldsCorrectly() {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;
        InboundVerifyMatchingServiceRequest request = anInboundVerifyMatchingServiceRequest(matchingDataset, levelOfAssurance);

        VerifyMatchingDatasetDto verifyMatchingDatasetDto = mock(VerifyMatchingDatasetDto.class);
        when(matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset)).thenReturn(verifyMatchingDatasetDto);

        String hashedPid = "a-hashed-pid";
        when(userIdHashFactory.hashId(any(), any(), any())).thenReturn(hashedPid);

        VerifyMatchingServiceRequestDto requestDto = (VerifyMatchingServiceRequestDto) verifyOnlyMapper.map(request);

        assertMatchingServiceRequestFields(requestDto, levelOfAssuranceDto, false, hashedPid, request.getId());
        assertThat(requestDto.getMatchingDataset()).isEqualTo(verifyMatchingDatasetDto);
    }

    @Test
    public void map_verify_shouldMapTheFieldsToUniversalDtoWhenEidasEnabled() {
        MatchingDataset matchingDataset = aMatchingDataset().build();
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;
        InboundVerifyMatchingServiceRequest request = anInboundVerifyMatchingServiceRequest(matchingDataset, levelOfAssurance);

        UniversalMatchingDatasetDto universalMatchingDatasetDto = mock(UniversalMatchingDatasetDto.class);
        when(matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset)).thenReturn(universalMatchingDatasetDto);

        String hashedPid = "a-hashed-pid";
        when(userIdHashFactory.hashId(any(), any(), any())).thenReturn(hashedPid);

        UniversalMatchingServiceRequestDto requestDto = (UniversalMatchingServiceRequestDto) eidasEnabledMapper.map(request);

        assertMatchingServiceRequestFields(requestDto, levelOfAssuranceDto, false, hashedPid, request.getId());
        assertThat(requestDto.getMatchingDataset()).isEqualTo(universalMatchingDatasetDto);
    }

    @Test
    public void map_eidas_shouldMapTheFieldsCorrectly() {
        EidasMatchingDataset eidasMatchingDataset = anEidasMatchingDataset().build();
        UniversalMatchingDatasetDto expectedMatchingDatasetDto = mock(UniversalMatchingDatasetDto.class);
        LevelOfAssuranceDto levelOfAssuranceDto = LevelOfAssuranceDto.LEVEL_2;
        InboundEidasMatchingServiceRequest request = anInboundEidasMatchingServiceRequest(eidasMatchingDataset);

        String hashedPid = "a-hashed-pid";
        when(userIdHashFactory.hashId(any(), any(), any())).thenReturn(hashedPid);
        when(matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(eidasMatchingDataset))
                .thenReturn(expectedMatchingDatasetDto);

        UniversalMatchingServiceRequestDto requestDto = eidasEnabledMapper.map(request);

        assertMatchingServiceRequestFields(requestDto, levelOfAssuranceDto, false, hashedPid, request.getId());
        assertThat(requestDto.getMatchingDataset()).isEqualTo(expectedMatchingDatasetDto);
    }

    @Test
    public void map_verify_shouldAddCycle3DatasetWhenPresent() {
        Cycle3Dataset cycle3Data = aCycle3Dataset().build();
        Cycle3DatasetDto cycle3DatasetDto = aCycle3DatasetDto().build();
        InboundVerifyMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withCycle3DataAssertion(aHubAssertion().withCycle3Data(cycle3Data).build())
                .buildForVerify();

        VerifyMatchingServiceRequestDto requestDto = (VerifyMatchingServiceRequestDto) verifyOnlyMapper.map(request);

        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(true);
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).isEqualTo(cycle3DatasetDto.getAttributes());
    }

    @Test
    public void map_verify_shouldAddCycle3DatasetToUniversalDtoWhenPresent() {
        Cycle3Dataset cycle3Data = aCycle3Dataset().build();
        Cycle3DatasetDto cycle3DatasetDto = aCycle3DatasetDto().build();
        InboundVerifyMatchingServiceRequest request = anInboundMatchingServiceRequest()
                .withCycle3DataAssertion(aHubAssertion().withCycle3Data(cycle3Data).build())
                .buildForVerify();

        UniversalMatchingServiceRequestDto requestDto = (UniversalMatchingServiceRequestDto) eidasEnabledMapper.map(request);

        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(true);
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).isEqualTo(cycle3DatasetDto.getAttributes());
    }

    private InboundVerifyMatchingServiceRequest anInboundVerifyMatchingServiceRequest(MatchingDataset matchingDataset, AuthnContext levelOfAssurance) {
        IdentityProviderAuthnStatement idaAuthnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(levelOfAssurance)
                .build();
        return anInboundMatchingServiceRequest()
                .withMatchingDatasetAssertion(anIdentityProviderAssertion().withMatchingDataset(matchingDataset).build())
                .withAuthnStatementAssertion(anIdentityProviderAssertion().withAuthnStatement(idaAuthnStatement).build())
                .buildForVerify();
    }

    private InboundEidasMatchingServiceRequest anInboundEidasMatchingServiceRequest(EidasMatchingDataset eidasMatchingDataset) {
        ProxyNodeAssertion proxyNodeAssertion = anProxyNodeAssertion()
                .withIssuer("an-issuer")
                .withLevelOfAssurance(LevelOfAssurance.SUBSTANTIAL)
                .withPersonIdentifier("a-person-identifier")
                .withEidasMatchingDataset(eidasMatchingDataset)
                .build();
        return anInboundMatchingServiceRequest()
                .withProxyNodeAssertion(proxyNodeAssertion)
                .buildForEidas();
    }

    private void assertMatchingServiceRequestFields(MatchingServiceRequestDto requestDto,
                                                    LevelOfAssuranceDto levelOfAssuranceDto,
                                                    boolean isCycle3Present,
                                                    String hashedPid,
                                                    String requestId) {
        assertThat(requestDto.getCycle3Dataset().isPresent()).isEqualTo(isCycle3Present);
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(levelOfAssuranceDto);
        assertThat(requestDto.getHashedPid()).isEqualTo(hashedPid);
        assertThat(requestDto.getMatchId()).isEqualTo(requestId);
    }
}
