package uk.gov.ida.matchingserviceadapter.proxies;


import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

public interface MatchingServiceProxy {

    MatchingServiceResponseDto makeMatchingServiceRequest(MatchingServiceRequestDto attributeQuery);

    UnknownUserCreationResponseDto makeUnknownUserCreationRequest(UnknownUserCreationRequestDto attributeQuery);

}
