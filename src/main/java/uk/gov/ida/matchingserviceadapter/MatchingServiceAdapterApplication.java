package uk.gov.ida.matchingserviceadapter;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.bundles.MonitoringBundle;
import uk.gov.ida.bundles.ServiceStatusBundle;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionExceptionMapper;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlOverSoapExceptionMapper;
import uk.gov.ida.matchingserviceadapter.healthcheck.MatchingServiceAdapterHealthCheck;
import uk.gov.ida.matchingserviceadapter.resources.LocalMetadataResource;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResource;
import uk.gov.ida.matchingserviceadapter.resources.UnknownUserAttributeQueryResource;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

import static com.hubspot.dropwizard.guicier.GuiceBundle.defaultBuilder;

public class MatchingServiceAdapterApplication extends Application<MatchingServiceAdapterConfiguration> {

    private MetadataResolverBundle<MatchingServiceAdapterConfiguration> metadataResolverBundle;

    public static void main(String[] args) {
        // running this method here stops the odd exceptions/double-initialisation that happens without it
        // - it's the same fix that was required in the tests...
        JerseyGuiceUtils.reset();

        new MatchingServiceAdapterApplication().runRuntimeException(args);
    }

    @Override
    public String getName() {
        return "Matching Service Adapter";
    }

    @Override
    public final void initialize(Bootstrap<MatchingServiceAdapterConfiguration> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new Jdk8Module());

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );


        metadataResolverBundle = new MetadataResolverBundle<>(MatchingServiceAdapterConfiguration::getMetadataConfiguration);
        bootstrap.addBundle(metadataResolverBundle);


        // Built in Stage.DEVELOPMENT for lazy loading of Singleton objects,
        // this is required as we have Singletons which require the jersey
        // Environment (not available at initialize)
        //
        // See this issue for updates on a lazy Singleton scope
        //
        // https://github.com/google/guice/issues/357
        GuiceBundle<MatchingServiceAdapterConfiguration> guiceBundle = defaultBuilder(MatchingServiceAdapterConfiguration.class)
                .modules(new MatchingServiceAdapterModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MetadataResolver.class).toProvider(metadataResolverBundle.getMetadataResolverProvider()).asEagerSingleton();
                        bind(ExplicitKeySignatureTrustEngine.class).toProvider(metadataResolverBundle.getSignatureTrustEngineProvider()).asEagerSingleton();
                        bind(MetadataCredentialResolver.class).toProvider(metadataResolverBundle.getMetadataCredentialResolverProvider()).asEagerSingleton();
                    }
                })
                .build();
        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(new LoggingBundle());
        bootstrap.addBundle(new MonitoringBundle());
        bootstrap.addBundle(new ServiceStatusBundle());
    }

    @Override
    public final void run(MatchingServiceAdapterConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();

        environment.getObjectMapper().setDateFormat(new ISO8601DateFormat());

        environment.jersey().register(LocalMetadataResource.class);
        environment.jersey().register(MatchingServiceResource.class);
        environment.jersey().register(UnknownUserAttributeQueryResource.class);

        environment.jersey().register(SamlOverSoapExceptionMapper.class);
        environment.jersey().register(ExceptionExceptionMapper.class);


        environment.healthChecks().register("VerifyMetadataHealthCheck", new MetadataHealthCheck(metadataResolverBundle.getMetadataResolver(), configuration.getMetadataConfiguration().getExpectedEntityId()));

        MatchingServiceAdapterHealthCheck healthCheck = new MatchingServiceAdapterHealthCheck();
        environment.healthChecks().register(healthCheck.getName(), healthCheck);
    }

    private void runRuntimeException(String[] arguments) {
        try {
            run(arguments);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
