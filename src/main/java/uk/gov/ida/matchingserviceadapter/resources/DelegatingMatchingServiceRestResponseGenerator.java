package uk.gov.ida.matchingserviceadapter.resources;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DelegatingMatchingServiceRestResponseGenerator implements MatchingServiceRestResponseGenerator<MatchingServiceResponse> {
    private final Map<Class<?>, MatchingServiceRestResponseGenerator> delegates;

    public DelegatingMatchingServiceRestResponseGenerator(ImmutableMap<Class<?>, MatchingServiceRestResponseGenerator> delegates) {
        this.delegates = delegates;
    }

    public Map<Class<?>, MatchingServiceRestResponseGenerator> getDelegates() {
        return delegates;
    }

    @Override
    public Response generateResponse(MatchingServiceResponse matchingServiceResponse) {
        MatchingServiceRestResponseGenerator delegateRenderer = delegates.get(matchingServiceResponse.getClass());
        if (delegateRenderer == null) {
            throw new IllegalStateException(String.format("No delegate found for matching service response [%s]", matchingServiceResponse.getClass()));
        }

        return delegateRenderer.generateResponse(matchingServiceResponse);
    }
}
