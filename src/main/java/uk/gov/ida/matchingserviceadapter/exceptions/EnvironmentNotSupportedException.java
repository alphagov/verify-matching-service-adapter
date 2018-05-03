package uk.gov.ida.matchingserviceadapter.exceptions;

public class EnvironmentNotSupportedException extends RuntimeException {
    public EnvironmentNotSupportedException(String message) {
        super(message);
    }
}
