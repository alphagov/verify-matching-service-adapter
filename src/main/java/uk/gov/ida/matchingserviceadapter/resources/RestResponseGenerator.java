package uk.gov.ida.matchingserviceadapter.resources;

@FunctionalInterface
public interface RestResponseGenerator<I, R> {
    R generateResponse(I matchingServiceResponse);
}
