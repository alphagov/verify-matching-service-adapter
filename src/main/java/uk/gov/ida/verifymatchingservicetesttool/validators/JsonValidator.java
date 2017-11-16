package uk.gov.ida.verifymatchingservicetesttool.validators;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonValidator {

    public void validate(String validationErrorMessage, String jsonString) {
        try {
            new ObjectMapper().readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(validationErrorMessage + " Reason: " + e.getMessage(), e);
        }
    }
}
