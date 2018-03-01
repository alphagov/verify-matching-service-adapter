package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.builders.AddressDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueDtoBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.UniversalMatchingDatasetDtoBuilder.aUniversalMatchingDatasetDto;
import static uk.gov.ida.matchingserviceadapter.rest.JsonTestUtil.jsonFixture;

public class UniversalMatchingDatasetDtoTest {

    private ObjectMapper objectMapper;
    private static final DateTime date = DateTime.parse("2014-02-01T01:02:03.567Z");


    @Before
    public void setUp() {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson_withTwoAddresses() throws IOException {

        MatchingDatasetDto universalMatchingDatasetDto = createUniversalMatchingDatasetDto_twoAddresses(date);

        String serializedJson = objectMapper.writeValueAsString(universalMatchingDatasetDto);
        String expectedJson = jsonFixture(objectMapper, "universal-matching-dataset_two-addresses.json");

        assertThat(serializedJson).isEqualTo(expectedJson);
    }

    @Test
    public void shouldDeserializeFromJson_withTwoAddresses() throws Exception {
        UniversalMatchingDatasetDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "universal-matching-dataset_two-addresses.json"), UniversalMatchingDatasetDto.class);

        MatchingDatasetDto expectedValue = createUniversalMatchingDatasetDto_twoAddresses(date);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    @Test
    public void shouldSerializeToJson_withEmptyAddressesElement() throws IOException {

        MatchingDatasetDto universalMatchingDatasetDto = createUniversalMatchingDatasetDto_emptyAddressesElement(date);

        String serializedJson = objectMapper.writeValueAsString(universalMatchingDatasetDto);
        String expectedJson = jsonFixture(objectMapper, "universal-matching-dataset_empty-addresses-element.json");

        assertThat(serializedJson).isEqualTo(expectedJson);
    }

    @Test
    public void shouldDeserializeFromJson_withEmptyAddressesElement() throws Exception {
        UniversalMatchingDatasetDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "universal-matching-dataset_empty-addresses-element.json"), UniversalMatchingDatasetDto.class);

        MatchingDatasetDto expectedValue = createUniversalMatchingDatasetDto_emptyAddressesElement(date);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    @Test
    public void shouldSerializeToJson_withNoAddresses() throws IOException {

        MatchingDatasetDto universalMatchingDatasetDto = createUniversalMatchingDatasetDto_noAddresses(date);

        String serializedJson = objectMapper.writeValueAsString(universalMatchingDatasetDto);
        String expectedJson = jsonFixture(objectMapper, "universal-matching-dataset_no-addresses-element.json");

        assertThat(serializedJson).isEqualTo(expectedJson);
    }

    @Test
    public void shouldDeserializeFromJson_withNoAddresses() throws Exception {
        UniversalMatchingDatasetDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "universal-matching-dataset_no-addresses-element.json"), UniversalMatchingDatasetDto.class);

        MatchingDatasetDto expectedValue = createUniversalMatchingDatasetDto_noAddresses(date);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private MatchingDatasetDto createUniversalMatchingDatasetDto_twoAddresses(DateTime dateTime) {
        return getMatchingDatasetDtoBuilderWithBasicDetails(dateTime)
                .withAddressHistory(ImmutableList.of(getAddressDto("EC2", dateTime), getAddressDto("WC1", dateTime)))
                .build();
    }

    private MatchingDatasetDto createUniversalMatchingDatasetDto_emptyAddressesElement(DateTime dateTime) {
        return getMatchingDatasetDtoBuilderWithBasicDetails(dateTime)
                .withAddressHistory(Collections.emptyList())
                .build();
    }

    private MatchingDatasetDto createUniversalMatchingDatasetDto_noAddresses(DateTime dateTime) {
        return getMatchingDatasetDtoBuilderWithBasicDetails(dateTime)
                .omittingTheAddressHistory()
                .build();
    }

    private MatchingDatasetDtoBuilder<UniversalAddressDto> getMatchingDatasetDtoBuilderWithBasicDetails(DateTime dateTime) {
        return aUniversalMatchingDatasetDto()
                .addSurname(getSimpleMdsValue("walker", dateTime))
                .withDateOfBirth(getSimpleMdsValue(LocalDate.fromDateFields(dateTime.toDate()), dateTime))
                .withFirstname(getSimpleMdsValue("walker", dateTime))
                .withGender(getSimpleMdsValue(GenderDto.FEMALE, dateTime))
                .withMiddleNames(getSimpleMdsValue("walker", dateTime))
                .withSurnameHistory(
                        ImmutableList.of(
                                getSimpleMdsValue("smith", dateTime),
                                getSimpleMdsValue("walker", dateTime)
                        ));
    }

    private UniversalAddressDto getAddressDto(String postcode, DateTime dateTime) {
        return new AddressDtoBuilder()
                .withFromDate(dateTime)
                .withInternationalPostCode("123")
                .withLines(ImmutableList.of("a", "b")).withPostCode(postcode)
                .withToDate(dateTime)
                .withUPRN("urpn")
                .withVerified(true)
                .buildUniversalAddressDto();
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
