package uk.gov.ida.matchingserviceadapter.resources;

@FunctionalInterface
public interface ResponseGenerator<I, R> {
    R generateResponse(I matchingServiceResponse);
}
