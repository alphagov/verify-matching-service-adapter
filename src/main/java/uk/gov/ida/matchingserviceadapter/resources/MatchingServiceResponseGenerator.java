package uk.gov.ida.matchingserviceadapter.resources;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface MatchingServiceResponseGenerator<I extends MatchingServiceResponse> extends ResponseGenerator<I, Response> {
    Response generateResponse(I matchingServiceResponse);
}
