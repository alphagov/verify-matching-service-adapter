package uk.gov.ida.matchingserviceadapter.exceptions;

public class InvalidMatchingServiceResponseException extends RuntimeException {

    public InvalidMatchingServiceResponseException(String msg) {
        super(msg);
    }
}
