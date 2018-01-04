package uk.gov.ida.matchingserviceadapter.resources;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DelegatingMatchingServiceResponseGenerator implements MatchingServiceResponseGenerator<MatchingServiceResponse> {
    private final Map<Class<?>, MatchingServiceResponseGenerator> delegates;

    public DelegatingMatchingServiceResponseGenerator(ImmutableMap<Class<?>, MatchingServiceResponseGenerator> delegates) {
        this.delegates = delegates;
    }

    public Map<Class<?>, MatchingServiceResponseGenerator> getDelegates() {
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
