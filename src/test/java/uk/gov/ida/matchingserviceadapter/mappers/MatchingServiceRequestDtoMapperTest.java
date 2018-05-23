package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto.LEVEL_2;
import static uk.gov.ida.saml.core.test.builders.Cycle3DatasetBuilder.aCycle3Dataset;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceRequestDtoMapperTest {

    @Mock
    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    @Test
    public void shouldMapToVerifyMatchingServiceRequestWhenEidasDisabled() {
        MatchingDataset matchingDataset = mock(MatchingDataset.class);
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        AssertionData assertionData = new AssertionData("a-mds-issuer",
                levelOfAssurance,
                Optional.empty(),
                matchingDataset);

        VerifyMatchingDatasetDto verifyMatchingDatasetDto = mock(VerifyMatchingDatasetDto.class);
        when(matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset)).thenReturn(verifyMatchingDatasetDto);

        MatchingServiceRequestDto requestDto = new MatchingServiceRequestDtoMapper(matchingDatasetToMatchingDatasetDtoMapper, false)
                .map("a-request-id", "a-hashed-pid", assertionData);

        assertThat(requestDto).isInstanceOf(VerifyMatchingServiceRequestDto.class);
        assertThat(requestDto.getMatchId()).isEqualTo("a-request-id");
        assertThat(requestDto.getHashedPid()).isEqualTo("a-hashed-pid");
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(requestDto.getCycle3Dataset()).isEmpty();
    }
    @Test
    public void shouldMapToVerifyMatchingServiceRequestWhenEidasDisabledWithCycle3() {
        MatchingDataset matchingDataset = mock(MatchingDataset.class);
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        Cycle3Dataset cycle3Dataset = aCycle3Dataset().addCycle3Data("NI", "123456").build();
        AssertionData assertionData = new AssertionData("a-mds-issuer",
                levelOfAssurance,
                Optional.of(cycle3Dataset),
                matchingDataset);

        VerifyMatchingDatasetDto verifyMatchingDatasetDto = mock(VerifyMatchingDatasetDto.class);
        when(matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset)).thenReturn(verifyMatchingDatasetDto);

        MatchingServiceRequestDto requestDto = new MatchingServiceRequestDtoMapper(matchingDatasetToMatchingDatasetDtoMapper, false)
                .map("a-request-id", "a-hashed-pid", assertionData);

        assertThat(requestDto).isInstanceOf(VerifyMatchingServiceRequestDto.class);
        assertThat(requestDto.getMatchId()).isEqualTo("a-request-id");
        assertThat(requestDto.getHashedPid()).isEqualTo("a-hashed-pid");
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(requestDto.getCycle3Dataset()).isPresent();
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).containsOnlyKeys("NI");
        assertThat(requestDto.getCycle3Dataset().get().getAttributes().get("NI")).isEqualTo("123456");
    }

    @Test
    public void shouldMapToUniversalMatchingServiceRequestWhenEidasEnabled() {
        MatchingDataset matchingDataset = mock(MatchingDataset.class);
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        AssertionData assertionData = new AssertionData("a-mds-issuer",
                levelOfAssurance,
                Optional.empty(),
                matchingDataset);


        MatchingServiceRequestDto requestDto = new MatchingServiceRequestDtoMapper(matchingDatasetToMatchingDatasetDtoMapper, true)
                .map("a-request-id", "a-hashed-pid", assertionData);

        assertThat(requestDto).isInstanceOf(UniversalMatchingServiceRequestDto.class);
        assertThat(requestDto.getMatchId()).isEqualTo("a-request-id");
        assertThat(requestDto.getHashedPid()).isEqualTo("a-hashed-pid");
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(requestDto.getCycle3Dataset()).isEmpty();
    }
    @Test
    public void shouldMapToUniversalMatchingServiceRequestWhenEidasEnabledWithCycle3() {
        MatchingDataset matchingDataset = mock(MatchingDataset.class);
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        Cycle3Dataset cycle3Dataset = aCycle3Dataset().addCycle3Data("NI", "123456").build();
        AssertionData assertionData = new AssertionData("a-mds-issuer",
                levelOfAssurance,
                Optional.of(cycle3Dataset),
                matchingDataset);


        MatchingServiceRequestDto requestDto = new MatchingServiceRequestDtoMapper(matchingDatasetToMatchingDatasetDtoMapper, true)
                .map("a-request-id", "a-hashed-pid", assertionData);

        assertThat(requestDto).isInstanceOf(UniversalMatchingServiceRequestDto.class);
        assertThat(requestDto.getMatchId()).isEqualTo("a-request-id");
        assertThat(requestDto.getHashedPid()).isEqualTo("a-hashed-pid");
        assertThat(requestDto.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(requestDto.getCycle3Dataset()).isPresent();
        assertThat(requestDto.getCycle3Dataset().get().getAttributes()).containsOnlyKeys("NI");
        assertThat(requestDto.getCycle3Dataset().get().getAttributes().get("NI")).isEqualTo("123456");
    }
}
