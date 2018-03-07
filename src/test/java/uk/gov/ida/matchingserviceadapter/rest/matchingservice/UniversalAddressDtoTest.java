package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.builders.AddressDtoBuilder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.rest.JsonTestUtil.jsonFixture;

public class UniversalAddressDtoTest {

    private ObjectMapper objectMapper;
    private static final DateTime fromDate = DateTime.parse("2010-06-29T23:20:00.000Z");
    private static final DateTime toDate = DateTime.parse("2014-02-01T01:02:03.567Z");

    @Before
    public void setUp() {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {

        UniversalAddressDto originalDto = createUniversalAddressDto(fromDate, toDate);

        String serializedJson = objectMapper.writeValueAsString(originalDto);

        UniversalAddressDto reserializedDto = objectMapper.readValue(serializedJson, UniversalAddressDto.class);

        assertThat(reserializedDto).isEqualTo(originalDto);
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        UniversalAddressDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "universal-address.json"), UniversalAddressDto.class);

        UniversalAddressDto expectedValue = createUniversalAddressDto(fromDate, toDate);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private UniversalAddressDto createUniversalAddressDto(DateTime from, DateTime to) {
        return new AddressDtoBuilder()
                .withFromDate(from)
                .withInternationalPostCode("EC-2")
                .withLines(ImmutableList.of("a", "b")).withPostCode("EC2")
                .withToDate(to)
                .withUPRN("uprn1234")
                .withVerified(true)
                .buildUniversalAddressDto();
    }

}