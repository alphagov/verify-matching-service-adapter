package uk.gov.ida.matchingserviceadapter.resources;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface MatchingServiceRestResponseGenerator<I extends MatchingServiceResponse> extends RestResponseGenerator<I, Response> {
    Response generateResponse(I matchingServiceResponse);
}
