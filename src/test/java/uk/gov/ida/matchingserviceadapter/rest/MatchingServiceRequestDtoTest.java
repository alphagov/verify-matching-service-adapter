package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.builders.AddressDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueDtoBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;

import java.io.IOException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

//
// These tests exist to prevent accidentally breaking our contract with the matching service. If they fail, ensure you
// are making changes in such a way that will not break our contract (i.e. use the expand/contract pattern); don't
// simply fix the test.
//
public class MatchingServiceRequestDtoTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {
        MatchingServiceRequestDto matchingServiceRequestDto = getMatchingServiceRequestDto();

        String jsonString = objectMapper.writeValueAsString(matchingServiceRequestDto);

        assertThat(jsonString).isEqualTo(jsonFixture("matching-service-request.json"));
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        MatchingServiceRequestDto deserializedValue =
                objectMapper.readValue(jsonFixture("matching-service-request.json"), MatchingServiceRequestDto.class);

        MatchingServiceRequestDto expectedValue = getMatchingServiceRequestDto();
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private MatchingServiceRequestDto getMatchingServiceRequestDto() {
        LevelOfAssuranceDto levelOfAssurance = LevelOfAssuranceDto.LEVEL_1;
        MatchingDatasetDto matchingDataset = getMatchingDataset(DateTime.parse("2014-02-01T01:02:03.567Z"));
        Cycle3DatasetDto cycle3DatasetDto = Cycle3DatasetDto.createFromData(ImmutableMap.of("NI", "1234"));
        String hashedPid = "8f2f8c23-f767-4590-aee9-0842f7f1e36d";
        String matchId = "cda6126c-9695-4051-ba6f-27a8938a0b03";
        return new MatchingServiceRequestDto(
                matchingDataset,
                Optional.of(cycle3DatasetDto),
                hashedPid,
                matchId,
                levelOfAssurance);
    }

    private MatchingDatasetDto getMatchingDataset(DateTime dateTime) {
        return new MatchingDatasetDtoBuilder()
                .addSurname(getSimpleMdsValue("walker", dateTime))
                .withAddressHistory(ImmutableList.of(getAddressDto("EC2", dateTime), getAddressDto("WC1", dateTime)))
                .withDateOfBirth(getSimpleMdsValue(LocalDate.fromDateFields(dateTime.toDate()), dateTime))
                .withFirstname(getSimpleMdsValue("walker", dateTime))
                .withGender(getSimpleMdsValue(GenderDto.FEMALE, dateTime))
                .withMiddleNames(getSimpleMdsValue("walker", dateTime))
                .withSurnameHistory(
                        ImmutableList.of(
                                getSimpleMdsValue("smith", dateTime),
                                getSimpleMdsValue("walker", dateTime)
                        ))
                .build();
    }

    private String jsonFixture(String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(fixture(filename), JsonNode.class));
    }

    private AddressDto getAddressDto(String postcode, DateTime dateTime) {
        return new AddressDtoBuilder()
                .withFromDate(dateTime)
                .withInternationalPostCode("123")
                .withLines(ImmutableList.of("a", "b")).withPostCode(postcode)
                .withToDate(dateTime)
                .withUPRN("urpn")
                .withVerified(true)
                .build();
    }

    private <T> SimpleMdsValueDto<T> getSimpleMdsValue(T value, DateTime dateTime) {
        return new SimpleMdsValueDtoBuilder<T>()
                .withFrom(dateTime)
                .withTo(dateTime)
                .withValue(value)
                .withVerifiedStatus(true)
                .build();
    }
}
