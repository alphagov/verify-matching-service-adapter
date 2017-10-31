package uk.gov.ida.matchingserviceadapter;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.base.Throwables;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.bundles.MonitoringBundle;
import uk.gov.ida.bundles.ServiceStatusBundle;
import uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterCoreBinder;
import uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterMetadataBinder;
import uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterPKIBinder;
import uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionExceptionMapper;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlOverSoapExceptionMapper;
import uk.gov.ida.matchingserviceadapter.healthcheck.MatchingServiceAdapterHealthCheck;
import uk.gov.ida.matchingserviceadapter.resources.LocalMetadataResource;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResource;
import uk.gov.ida.matchingserviceadapter.resources.UnknownUserAttributeQueryResource;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

public class MatchingServiceAdapterApplication extends Application<MatchingServiceAdapterConfiguration> {

    private MetadataResolverBundle<MatchingServiceAdapterConfiguration> metadataResolverBundle;

    public static void main(String[] args) {
        new MatchingServiceAdapterApplication().runRuntimeException(args);
    }

    @Override
    public String getName() {
        return "Matching Service Adapter";
    }

    @Override
    public final void initialize(Bootstrap<MatchingServiceAdapterConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new LoggingBundle());
        bootstrap.addBundle(new MonitoringBundle());
        bootstrap.addBundle(new ServiceStatusBundle());
    }

    @Override
    public final void run(MatchingServiceAdapterConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();

        environment.getObjectMapper().setDateFormat(new ISO8601DateFormat());

        environment.jersey().register(new MatchingServiceAdapterCoreBinder(configuration, environment));
        environment.jersey().register(new MatchingServiceAdapterMetadataBinder(configuration, environment));
        environment.jersey().register(new MatchingServiceAdapterSamlBinder(configuration));
        environment.jersey().register(new MatchingServiceAdapterPKIBinder(configuration));

        environment.jersey().register(LocalMetadataResource.class);
        environment.jersey().register(MatchingServiceResource.class);
        environment.jersey().register(UnknownUserAttributeQueryResource.class);

        environment.jersey().register(SamlOverSoapExceptionMapper.class);
        environment.jersey().register(ExceptionExceptionMapper.class);

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
