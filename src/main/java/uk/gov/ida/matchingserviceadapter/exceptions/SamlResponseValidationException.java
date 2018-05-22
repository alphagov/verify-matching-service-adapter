package uk.gov.ida.matchingserviceadapter.exceptions;

public class SamlResponseValidationException extends RuntimeException {

    public SamlResponseValidationException(String msg) {
        super(msg);
    }
}
