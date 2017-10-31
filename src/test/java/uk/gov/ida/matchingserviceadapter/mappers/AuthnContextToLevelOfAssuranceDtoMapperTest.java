package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.saml.core.domain.AuthnContext;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthnContextToLevelOfAssuranceDtoMapperTest {

    @Test
    public void test_expectedLevels() {
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_1)).isEqualTo(LevelOfAssuranceDto.LEVEL_1);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_2)).isEqualTo(LevelOfAssuranceDto.LEVEL_2);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_3)).isEqualTo(LevelOfAssuranceDto.LEVEL_3);
        assertThat(AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_4)).isEqualTo(LevelOfAssuranceDto.LEVEL_4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_unExpectedLevels() {
        AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.valueOf("LEVEL_11"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_unExpectedLevel_X() {
        AuthnContextToLevelOfAssuranceDtoMapper.map(AuthnContext.LEVEL_X);
    }

}