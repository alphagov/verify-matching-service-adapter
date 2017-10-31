package uk.gov.ida.matchingserviceadapter.exceptions;

public class InvalidSamlMetadataException extends RuntimeException {

    public InvalidSamlMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSamlMetadataException(String message) {
        super(message);
    }
}
