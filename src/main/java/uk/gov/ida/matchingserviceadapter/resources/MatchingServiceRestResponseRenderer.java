package uk.gov.ida.matchingserviceadapter.resources;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface MatchingServiceRestResponseRenderer<I extends MatchingServiceResponse> extends RestResponseRenderer<I, Response> {
    Response render(I matchingServiceResponse);
}
