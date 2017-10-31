package uk.gov.ida.matchingserviceadapter.exceptions;

public class FederationMetadataLoadingException extends Exception {
    @Override
    public String getMessage() {
        return "Unable to extract Verify hub entity descriptor from metadata.";
    }
}
