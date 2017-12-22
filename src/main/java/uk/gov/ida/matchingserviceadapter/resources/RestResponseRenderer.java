package uk.gov.ida.matchingserviceadapter.resources;

@FunctionalInterface
public interface RestResponseRenderer<I, R> {
    R render(I matchingServiceResponse);
}
