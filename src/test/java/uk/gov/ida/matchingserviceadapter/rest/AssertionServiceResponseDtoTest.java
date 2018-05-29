package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class AssertionServiceResponseDtoTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws Exception {
        String jsonString = objectMapper.writeValueAsString(MatchingServiceResponseDto.MATCH_RESPONSE);

        assertThat(jsonString).isEqualTo(jsonFixture("matching-service-response.json"));
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        MatchingServiceResponseDto deserializedValue =
                objectMapper.readValue(jsonFixture("matching-service-response.json"), MatchingServiceResponseDto.class);

        MatchingServiceResponseDto expectedValue = MatchingServiceResponseDto.MATCH_RESPONSE;
        assertThat(deserializedValue).isEqualToComparingFieldByField(expectedValue);
    }

    private String jsonFixture(String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(fixture(filename), JsonNode.class));
    }
}
