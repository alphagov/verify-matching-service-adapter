package uk.gov.ida.matchingserviceadapter.exceptions;

public class CouldNotSignMetadataException extends RuntimeException {
    public CouldNotSignMetadataException(Exception e) {
        super(e);
    }
}
