package uk.gov.ida.matchingserviceadapter.proxies;


import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

public interface MatchingServiceProxy {
    MatchingServiceResponseDto makeMatchingServiceRequest(VerifyMatchingServiceRequestDto attributeQuery);
    UnknownUserCreationResponseDto makeUnknownUserCreationRequest(UnknownUserCreationRequestDto attributeQuery);
}
