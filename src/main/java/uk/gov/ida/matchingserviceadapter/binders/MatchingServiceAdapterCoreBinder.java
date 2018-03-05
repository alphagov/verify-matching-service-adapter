package uk.gov.ida.matchingserviceadapter.binders;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.controllogic.ServiceLocator;
import uk.gov.ida.matchingserviceadapter.controllogic.UnknownUserAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionResponseFactory;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingDatasetToMatchingDatasetDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxyImpl;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;
import uk.gov.ida.matchingserviceadapter.resources.DelegatingMatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.injectors.EidasMatchingServiceFactory;
import uk.gov.ida.matchingserviceadapter.saml.injectors.MatchingServiceDelegatesFactory;
import uk.gov.ida.matchingserviceadapter.saml.injectors.ServicesMapFactory;
import uk.gov.ida.matchingserviceadapter.services.DelegatingMatchingService;
import uk.gov.ida.matchingserviceadapter.services.EidasMatchingService;
import uk.gov.ida.matchingserviceadapter.services.HealthCheckMatchingService;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;
import uk.gov.ida.matchingserviceadapter.services.VerifyMatchingService;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import java.util.Map;
import java.util.Optional;

public class MatchingServiceAdapterCoreBinder extends AbstractBinder {

    public static final String MATCHING_SERVICE_CLIENT = "MatchingServiceClient";

    private final MatchingServiceAdapterConfiguration configuration;
    private final Environment environment;

    public MatchingServiceAdapterCoreBinder(MatchingServiceAdapterConfiguration configuration,
                                            Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        // journey / service
        bind(ExceptionResponseFactory.class).to(ExceptionResponseFactory.class);
        bind(IdGenerator.class).to(IdGenerator.class);
        bind(InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper.class).to(InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper.class);
        bind(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class).to(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class);
        bind(MatchingServiceAdapterMetadataRepository.class).to(MatchingServiceAdapterMetadataRepository.class);
        bind(DocumentToInboundMatchingServiceRequestMapper.class).to(DocumentToInboundMatchingServiceRequestMapper.class);
        bind(MatchingServiceAssertionFactory.class).to(MatchingServiceAssertionFactory.class);
        bind(UserAccountCreationAttributeExtractor.class).to(UserAccountCreationAttributeExtractor.class);
        bind(UnknownUserAttributeQueryHandler.class).to(UnknownUserAttributeQueryHandler.class);
        bind(MatchingServiceAttributeQueryHandler.class).to(MatchingServiceAttributeQueryHandler.class);
        bind(MatchingDatasetToMatchingDatasetDtoMapper.class).to(MatchingDatasetToMatchingDatasetDtoMapper.class);
        bind(UserIdHashFactory.class).to(UserIdHashFactory.class);

        // clients / proxies
        bind(MatchingServiceProxyImpl.class).to(MatchingServiceProxy.class).in(Singleton.class);
        Client matchingServiceClient = new JerseyClientBuilder(environment).using(configuration.getMatchingServiceClientConfiguration()).build(MATCHING_SERVICE_CLIENT);
        bind(new JsonClient(new ErrorHandlingClient(matchingServiceClient), new JsonResponseProcessor(environment.getObjectMapper()))).named(MATCHING_SERVICE_CLIENT).to(JsonClient.class);

        // others
        final SoapMessageManager soapMessageManager = new SoapMessageManager();
        bind(soapMessageManager).to(SoapMessageManager.class);
        final ManifestReader manifestReader = new ManifestReader();
        bind(manifestReader).to(ManifestReader.class);

        // matching services
        bind(DelegatingMatchingService.class).to(MatchingService.class).in(Singleton.class);
        bind(DelegatingMatchingServiceResponseGenerator.class).to(new TypeLiteral<MatchingServiceResponseGenerator<MatchingServiceResponse>>() {}).in(Singleton.class);
        bind(HealthCheckMatchingService.class).to(HealthCheckMatchingService.class).in(Singleton.class);
        bind(VerifyMatchingService.class).to(VerifyMatchingService.class).in(Singleton.class);
        // can't use ? extends MatchingServiceResponse here
        bindFactory(MatchingServiceDelegatesFactory.class).to(new TypeLiteral<Map<Class<MatchingServiceResponse>, MatchingServiceResponseGenerator<MatchingServiceResponse>>>() {});
        bindFactory(EidasMatchingServiceFactory.class).to(new TypeLiteral<Optional<EidasMatchingService>>() {});
        bindFactory(ServicesMapFactory.class).to(new TypeLiteral<ServiceLocator<MatchingServiceRequestContext, MatchingService>>() {});

    }

}


