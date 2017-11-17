package uk.gov.ida.matchingserviceadapter.services;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

public interface MatchingService {
    MatchingServiceResponse handle(MatchingServiceRequestContext request);
}
