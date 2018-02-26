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

    @Before
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {

        DateTime fromDate = DateTime.parse("2010-06-30T01:20+02:00");
        DateTime toDate = DateTime.parse("2014-02-01T01:02:03.567Z");

        AddressDtoBuilder addressDtoBuilder = new AddressDtoBuilder();
        UniversalAddressDto universalAddressDto = addressDtoBuilder
                .withFromDate(fromDate)
                .withInternationalPostCode("EC-2")
                .withLines(ImmutableList.of("a", "b")).withPostCode("EC2")
                .withToDate(toDate)
                .withUPRN("uprn1234")
                .withVerified(true)
                .buildUniversalAddressDto();

        String serializedJson = objectMapper.writeValueAsString(universalAddressDto);
        String expectedJson = jsonFixture(objectMapper, "universal-address.json");

        assertThat(serializedJson).isEqualTo(expectedJson);
    }

}