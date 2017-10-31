package uk.gov.ida.matchingserviceadapter.rest;

public interface Urls {

    String SOAP_TIMED_GROUP = "SOAP";

    interface MatchingServiceAdapterUrls {
        String MATCHING_SERVICE_ROOT = "/matching-service";
        String MATCHING_SERVICE_METADATA_PATH = "/SAML2/metadata";
        String MATCHING_SERVICE_METADATA_RESOURCE = MATCHING_SERVICE_ROOT + "/SAML2/metadata";
        String MATCHING_SERVICE_MATCH_REQUEST_PATH = "/POST";
        String UNKNOWN_USER_ATTRIBUTE_QUERY_PATH = "/unknown-user-attribute-query";
    }
}