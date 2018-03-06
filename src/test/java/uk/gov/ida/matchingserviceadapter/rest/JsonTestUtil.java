package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static io.dropwizard.testing.FixtureHelpers.fixture;

public class JsonTestUtil {

    public static String jsonFixture(ObjectMapper objectMapper,  String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(fixture(filename), JsonNode.class));
    }

}
