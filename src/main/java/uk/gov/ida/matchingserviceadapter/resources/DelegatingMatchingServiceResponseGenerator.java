package uk.gov.ida.matchingserviceadapter.resources;

import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

public class DelegatingMatchingServiceResponseGenerator implements MatchingServiceResponseGenerator<MatchingServiceResponse> {

    private final Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> delegates;

    @Inject
    public DelegatingMatchingServiceResponseGenerator(Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> delegates) {
        this.delegates = delegates;
    }

    public Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> getDelegates() {
        return delegates;
    }

    @Override
    public Response generateResponse(MatchingServiceResponse matchingServiceResponse) {
        MatchingServiceResponseGenerator delegateGenerator = delegates.get(matchingServiceResponse.getClass());
        if (delegateGenerator == null) {
            throw new IllegalStateException(String.format("No delegate found for matching service response [%s]", matchingServiceResponse.getClass()));
        }

        return delegateGenerator.generateResponse(matchingServiceResponse);
    }
}
