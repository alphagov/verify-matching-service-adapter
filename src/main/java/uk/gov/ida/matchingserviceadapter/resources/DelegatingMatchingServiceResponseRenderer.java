package uk.gov.ida.matchingserviceadapter.resources;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DelegatingMatchingServiceResponseRenderer implements MatchingServiceResponseRenderer<MatchingServiceResponse> {
    private final Map<Class<?>, MatchingServiceResponseRenderer> delegates;

    public DelegatingMatchingServiceResponseRenderer(ImmutableMap<Class<?>, MatchingServiceResponseRenderer> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Response render(MatchingServiceResponse matchingServiceResponse) {
        return delegates.get(matchingServiceResponse.getClass()).render(matchingServiceResponse);
    }
}
