package uk.gov.ida.matchingserviceadapter.services;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.TranslatedAttributeQueryRequest;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

public interface MatchingService {
    TranslatedAttributeQueryRequest translate(MatchingServiceRequestContext request);
    MatchingServiceResponse createOutboundResponse(MatchingServiceRequestContext requestContext, TranslatedAttributeQueryRequest request, MatchingServiceResponseDto response);
    //OutboundResponseFromUnknownUserCreationService createOutboundResponse(MatchingServiceRequestContext requestContext, TranslatedAttributeQueryAccountCreationRequest request, UnknownUserCreationResponseDto response);
}
