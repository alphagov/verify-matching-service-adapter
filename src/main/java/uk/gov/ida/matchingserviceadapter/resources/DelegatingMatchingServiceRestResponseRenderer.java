package uk.gov.ida.matchingserviceadapter.resources;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DelegatingMatchingServiceRestResponseRenderer implements MatchingServiceRestResponseRenderer<MatchingServiceResponse> {
    private final Map<Class<?>, MatchingServiceRestResponseRenderer> delegates;

    public DelegatingMatchingServiceRestResponseRenderer(ImmutableMap<Class<?>, MatchingServiceRestResponseRenderer> delegates) {
        this.delegates = delegates;
    }

    public Map<Class<?>, MatchingServiceRestResponseRenderer> getDelegates() {
        return delegates;
    }

    @Override
    public Response render(MatchingServiceResponse matchingServiceResponse) {
        MatchingServiceRestResponseRenderer delegateRenderer = delegates.get(matchingServiceResponse.getClass());
        if (delegateRenderer == null) {
            throw new IllegalStateException(String.format("No delegate found for matching service response [%s]", matchingServiceResponse.getClass()));
        }

        return delegateRenderer.render(matchingServiceResponse);
    }
}
