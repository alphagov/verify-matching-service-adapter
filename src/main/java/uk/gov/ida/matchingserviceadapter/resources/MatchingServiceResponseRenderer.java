package uk.gov.ida.matchingserviceadapter.resources;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface MatchingServiceResponseRenderer<I extends MatchingServiceResponse> extends ResponseRenderer<I, Response> {
    Response render(I matchingServiceResponse);
}
