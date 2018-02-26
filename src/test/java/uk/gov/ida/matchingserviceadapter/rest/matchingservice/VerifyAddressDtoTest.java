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

public class VerifyAddressDtoTest {

    private ObjectMapper objectMapper;
    private static final DateTime fromDate = DateTime.parse("2010-06-29T23:20:00.000Z");
    private static final DateTime toDate = DateTime.parse("2014-02-01T01:02:03.567Z");

    @Before
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {

        VerifyAddressDto verifyAddressDto = createVerifyAddressDto(fromDate, toDate);

        String serializedJson = objectMapper.writeValueAsString(verifyAddressDto);
        String expectedJson = jsonFixture(objectMapper, "verify-address.json");

        assertThat(serializedJson).isEqualTo(expectedJson);
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        VerifyAddressDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "verify-address.json"), VerifyAddressDto.class);

        VerifyAddressDto expectedValue = createVerifyAddressDto(fromDate, toDate);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private VerifyAddressDto createVerifyAddressDto(DateTime fromDate, DateTime toDate) {
        return new AddressDtoBuilder()
                .withFromDate(fromDate)
                .withInternationalPostCode("EC-2")
                .withLines(ImmutableList.of("a", "b")).withPostCode("EC2")
                .withToDate(toDate)
                .withUPRN("uprn1234")
                .withVerified(true)
                .buildVerifyAddressDto();
    }

}