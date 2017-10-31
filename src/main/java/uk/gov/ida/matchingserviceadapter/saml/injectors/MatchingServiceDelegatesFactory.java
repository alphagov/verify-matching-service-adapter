package uk.gov.ida.matchingserviceadapter.saml.injectors;

import com.google.common.collect.ImmutableMap;
import org.glassfish.hk2.api.Factory;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.resources.HealthCheckResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.VerifyMatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class MatchingServiceDelegatesFactory implements Factory<Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>>> {

    private final Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> matchingServiceDelegates;

    @Inject
    public MatchingServiceDelegatesFactory(SoapMessageManager soapMessageManager,
                                           ManifestReader manifestReader,
                                           Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer,
                                           Function<OutboundResponseFromMatchingService, Element> responseElementTransformer) {

        matchingServiceDelegates = ImmutableMap.of(
                HealthCheckMatchingServiceResponse.class,
                new HealthCheckResponseGenerator(soapMessageManager, healthCheckResponseTransformer, manifestReader),
                VerifyMatchingServiceResponse.class,
                new VerifyMatchingServiceResponseGenerator(soapMessageManager, responseElementTransformer)
        );
    }

    @Override
    public Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> provide() {
        return matchingServiceDelegates;
    }

    @Override
    public void dispose(Map<Class<? extends MatchingServiceResponse>, MatchingServiceResponseGenerator<? extends MatchingServiceResponse>> instance) {
        // do nothing
    }

}
