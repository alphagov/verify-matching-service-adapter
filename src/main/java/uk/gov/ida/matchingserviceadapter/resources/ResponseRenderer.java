package uk.gov.ida.matchingserviceadapter.resources;

@FunctionalInterface
public interface ResponseRenderer<I, R> {
    R render(I matchingServiceResponse);
}
