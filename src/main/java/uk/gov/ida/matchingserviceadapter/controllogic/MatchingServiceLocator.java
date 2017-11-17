package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class MatchingServiceLocator implements ServiceLocator<MatchingServiceRequestContext, MatchingService> {

    private final Map<Predicate<MatchingServiceRequestContext>, MatchingService> dispatcher;

    public MatchingServiceLocator(ImmutableMap<Predicate<MatchingServiceRequestContext>, MatchingService> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public MatchingService findServiceFor(MatchingServiceRequestContext request) {
        Optional<Map.Entry<Predicate<MatchingServiceRequestContext>, MatchingService>> service = dispatcher.entrySet().stream().filter(pair -> pair.getKey().test(request)).findFirst();
        return service.orElseThrow(() -> new RuntimeException(String.format("No service found to match ", request))).getValue();
    }
}
