package uk.gov.ida.matchingserviceadapter.saml.injectors;

import com.google.common.collect.ImmutableMap;
import org.glassfish.hk2.api.Factory;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceLocator;
import uk.gov.ida.matchingserviceadapter.controllogic.ServiceLocator;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.repositories.ResolverBackedMetadataRepository;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributesBasedAttributeQueryDiscriminator;
import uk.gov.ida.matchingserviceadapter.services.EidasMatchingService;
import uk.gov.ida.matchingserviceadapter.services.HealthCheckMatchingService;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;
import uk.gov.ida.matchingserviceadapter.services.VerifyMatchingService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterMetadataBinder.COUNTRY_METADATA_RESOLVER;

@Singleton
public class ServicesMapFactory implements Factory<ServiceLocator<MatchingServiceRequestContext, MatchingService>> {

    private final Map<Predicate<MatchingServiceRequestContext>, MatchingService> servicesMap = new LinkedHashMap<>();

    @Inject
    public ServicesMapFactory(HealthCheckMatchingService healthCheckMatchingService,
                              VerifyMatchingService verifyMatchingService,
                              Optional<EidasMatchingService> eidasMatchingService,
                              @Named(COUNTRY_METADATA_RESOLVER) Optional<MetadataResolver> countryMetadataResolver) {

        servicesMap.put((MatchingServiceRequestContext ctx) -> ctx.getAssertions().isEmpty(), healthCheckMatchingService);
        countryMetadataResolver.ifPresent(countryResolver -> servicesMap.put(new EidasAttributesBasedAttributeQueryDiscriminator(new ResolverBackedMetadataRepository(countryMetadataResolver.get())), eidasMatchingService.get()));
        servicesMap.put(ctx -> true, verifyMatchingService);
    }

    @Override
    public ServiceLocator<MatchingServiceRequestContext, MatchingService> provide() {
        return new MatchingServiceLocator(ImmutableMap.copyOf(servicesMap));
    }

    @Override
    public void dispose(ServiceLocator<MatchingServiceRequestContext, MatchingService> instance) {
        // do nothing
    }
}
