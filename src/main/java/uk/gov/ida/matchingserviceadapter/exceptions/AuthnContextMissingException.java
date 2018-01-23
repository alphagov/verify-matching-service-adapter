package uk.gov.ida.matchingserviceadapter.exceptions;

public class AuthnContextMissingException extends RuntimeException {
    public AuthnContextMissingException(String message) {
        super(message);
    }
}
