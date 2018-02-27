package uk.gov.ida.verifymatchingservicetesttool.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

public class JsonValidator {

    public void validate(String validationErrorMessage, String jsonString) {
        wellFormedValidation(validationErrorMessage, jsonString);
        schemaValidation(validationErrorMessage, jsonString);
    }

    private void wellFormedValidation(String validationErrorMessage, String jsonString) {
        try {
            new ObjectMapper().readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(validationErrorMessage + " Reason: " + e.getMessage(), e);
        }
    }

    private void schemaValidation(String validationErrorMessage, String jsonString) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("legacy/matching-schema.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            SchemaLoader.load(rawSchema).validate(new JSONObject(jsonString));
        } catch (ValidationException e) {
            String reason = e.getAllMessages().stream()
                .collect(joining(lineSeparator()));
            throw new RuntimeException(validationErrorMessage + " JSON schema validation failed. Reason: " + lineSeparator() + reason, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
