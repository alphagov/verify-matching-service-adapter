package uk.gov.ida.matchingserviceadapter.saml;

public class SamlOverSoapException extends RuntimeException {

    private final String requestId;

    public SamlOverSoapException(String message, Throwable cause, String requestId) {
        super(message, cause);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
