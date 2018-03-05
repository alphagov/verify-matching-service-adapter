package uk.gov.ida.matchingserviceadapter.binders;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.saml.dropwizard.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.MetadataRefreshTask;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;

import java.io.PrintWriter;
import java.util.Optional;

public class MatchingServiceAdapterMetadataBinder extends AbstractBinder {

    public static final String VERIFY_METADATA_RESOLVER = "VerifyMetadataResolver";
    public static final String COUNTRY_METADATA_RESOLVER = "CountryMetadataResolver";
    public static final String HUB_FEDERATION_ID = "HubFederationId";

    private final MatchingServiceAdapterConfiguration configuration;
    private final Environment environment;

    public MatchingServiceAdapterMetadataBinder(MatchingServiceAdapterConfiguration configuration,
                                                Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(ExpiredCertificateMetadataFilter.class).to(ExpiredCertificateMetadataFilter.class);
        bind(configuration.getMetadataConfiguration().getHubFederationId()).named(HUB_FEDERATION_ID).to(String.class);

        // verify hub metadata
        bind(MetadataCertificatesRepository.class).named(VERIFY_METADATA_RESOLVER).to(MetadataCertificatesRepository.class);
        bind(configuration.getMetadataConfiguration()).to(MetadataConfiguration.class);
        final MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, configuration.getMetadataConfiguration());
        bind(metadataResolver).named(VERIFY_METADATA_RESOLVER).to(MetadataResolver.class);
        environment.healthChecks().register("VerifyMetadataHealthCheck", new MetadataHealthCheck(metadataResolver, configuration.getMetadataConfiguration().getExpectedEntityId()));
        addMetadataRefreshTask(metadataResolver, "metadata");

        // eidas metadata
        final Optional<MetadataResolver> countryMetadataResolver;
        if (configuration.isEidasEnabled()) {
            countryMetadataResolver = Optional.ofNullable(new DropwizardMetadataResolverFactory().createMetadataResolver(environment, configuration.getEuropeanIdentity().getMetadata()));
            environment.healthChecks().register("CountryMetadataHealthCheck", new MetadataHealthCheck(countryMetadataResolver.get(), configuration.getEuropeanIdentity().getMetadata().getExpectedEntityId()));
            addMetadataRefreshTask(metadataResolver, "eidas-metadata");
        } else {
            countryMetadataResolver = Optional.empty();
        }
        bind(countryMetadataResolver).named(COUNTRY_METADATA_RESOLVER).to(new TypeLiteral<Optional<MetadataResolver>>() {});

    }

    private void addMetadataRefreshTask(MetadataResolver metadataResolver, String name) {
        environment.admin().addTask(new Task(name + "-refresh") {
            @Override
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
                ((AbstractReloadingMetadataResolver) metadataResolver).refresh();
            }
        });
    }

}
