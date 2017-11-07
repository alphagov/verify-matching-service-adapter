package uk.gov.ida.matchingserviceadapter;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.joda.time.Duration;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PublicEncryptionKeyConfiguration;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFileInputStreamFactory;
import uk.gov.ida.common.shared.security.PublicKeyInputStreamFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.common.shared.security.verification.CertificateChainValidator;
import uk.gov.ida.common.shared.security.verification.PKIXParametersProvider;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.configuration.KeyPairConfiguration;
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.controllogic.UnknownUserAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionResponseFactory;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingDatasetToMatchingDatasetDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.AdapterToMatchingServiceHttpProxy;
import uk.gov.ida.matchingserviceadapter.proxies.AdapterToMatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.matchingserviceadapter.repositories.ResolverBackedMetadataRepository;
import uk.gov.ida.matchingserviceadapter.rest.MetadataPublicKeyStore;
import uk.gov.ida.matchingserviceadapter.rest.configuration.verification.FixedCertificateChainValidator;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.DiscriminatingAttributeQueryToInboundMSRequestTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributeQueryToInboundMatchingServiceRequestTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributesBasedAttributeQueryDiscriminator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.VerifyAttributeQueryToInboundMatchingServiceRequestTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.dropwizard.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;
import uk.gov.ida.saml.metadata.transformers.KeyDescriptorsUnmarshaller;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.CertificateChainEvaluableCriterion;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SigningKeyStore;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.truststore.KeyStoreLoader;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Encryption;
import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Signing;

class MatchingServiceAdapterModule extends AbstractModule {

    private static final String VERIFY_METADATA_RESOLVER = "VerifyMetadataResolver";

    private static final String COUNTRY_METADATA_RESOLVER = "CountryMetadataResolver";

    @Override
    protected void configure() {

        bind(PKIXParametersProvider.class).toInstance(new PKIXParametersProvider());
        bind(SoapMessageManager.class).toInstance(new SoapMessageManager());
        bind(X509CertificateFactory.class);
        bind(KeyStoreLoader.class);
        bind(IdGenerator.class);
        bind(IdaKeyStoreCredentialRetriever.class);
        bind(ExpiredCertificateMetadataFilter.class);
        bind(ExceptionResponseFactory.class);
        bind(InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper.class);
        bind(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class);
        bind(MatchingServiceAdapterMetadataRepository.class);
        bind(DocumentToInboundMatchingServiceRequestMapper.class);
        bind(IdGenerator.class);
        bind(MatchingServiceAssertionFactory.class);
        bind(UserAccountCreationAttributeExtractor.class);
        bind(UnknownUserAttributeQueryHandler.class);

        bind(SigningKeyStore.class).to(MetadataPublicKeyStore.class).in(Singleton.class);
        bind(EncryptionKeyStore.class).to(MetadataPublicKeyStore.class).in(Singleton.class);
        bind(PublicKeyInputStreamFactory.class).to(PublicKeyFileInputStreamFactory.class).in(Singleton.class);
        bind(AssertionLifetimeConfiguration.class).to(MatchingServiceAdapterConfiguration.class).in(Singleton.class);
        bind(AdapterToMatchingServiceProxy.class).to(AdapterToMatchingServiceHttpProxy.class).in(Singleton.class);
        bind(ManifestReader.class).toInstance(new ManifestReader());
        bind(MatchingDatasetToMatchingDatasetDtoMapper.class).toInstance(new MatchingDatasetToMatchingDatasetDtoMapper());
        bind(UserIdHashFactory.class).toInstance(new UserIdHashFactory());
    }

    @Provides
    @Singleton
    @Named("HubEntityId")
    public String getHubEntityId(MatchingServiceAdapterConfiguration configuration) {
        return configuration.getHubEntityId();
    }

    @Provides
    @Singleton
    @Named("HubFederationId")
    public String getHubFederationId(MatchingServiceAdapterConfiguration configuration) {
        return configuration.getMetadataConfiguration().getHubFederationId();
    }

    @Provides
    public EntityToEncryptForLocator getEntityToEncryptFor(@Named("HubEntityId") String hubEntityId) {
        return requestId -> hubEntityId;
    }

    @Provides
    @Singleton
    public OpenSamlXmlObjectFactory getOpenSamlXmlObjectFactory() {
        return new OpenSamlXmlObjectFactory();
    }

    @Provides
    @Singleton
    @Named("VerifyCertificateValidator")
    public CertificateValidator getCertificateValidator(
        X509CertificateFactory x509CertificateFactory,
        @Named("VerifyFixedCertificateChainValidator") FixedCertificateChainValidator fixedCertificateChainValidator) {
        return new CertificateValidator(x509CertificateFactory, fixedCertificateChainValidator);
    }

    @Provides
    @Singleton
    @Named("CountryCertificateValidator")
    public Optional<CertificateValidator> getCountryCertificateValidator(
        X509CertificateFactory x509CertificateFactory,
        @Named("CountryFixedCertificateChainValidator") Optional<FixedCertificateChainValidator> fixedCertificateChainValidator) {
        return fixedCertificateChainValidator.map(certificateChainValidator -> new CertificateValidator(x509CertificateFactory, certificateChainValidator));
    }

    @Provides
    @Singleton
    @Named("VerifyFixedCertificateChainValidator")
    public FixedCertificateChainValidator getFixedChainCertificateValidator(
        @Named("VerifyTrustStore") KeyStore keyStore,
        CertificateChainValidator certificateChainValidator) {
        return new FixedCertificateChainValidator(keyStore, certificateChainValidator);
    }

    @Provides
    @Singleton
    @Named("CountryFixedCertificateChainValidator")
    public Optional<FixedCertificateChainValidator> getCountryFixedChainCertificateValidator(
        @Named("CountryTrustStore") Optional<KeyStore> CountryTrustStore,
        CertificateChainValidator certificateChainValidator) {
        return CountryTrustStore.map(keyStore -> new FixedCertificateChainValidator(keyStore, certificateChainValidator));
    }

    @Provides
    @Singleton
    public CertificateChainValidator getCertificateChainValidator(PKIXParametersProvider pkixParametersProvider, X509CertificateFactory x509CertificateFactory) {
        return new CertificateChainValidator(pkixParametersProvider, x509CertificateFactory);
    }

    @Provides
    @Singleton
    @Named("VerifyCertificateChainEvaluableCriterion")
    public CertificateChainEvaluableCriterion getCertificateChainEvaluableCriterion(
        CertificateChainValidator certificateChainValidator,
        @Named("VerifyTrustStore") KeyStore keyStore) {
        return new CertificateChainEvaluableCriterion(certificateChainValidator, keyStore);
    }

    @Provides
    @Singleton
    public PublicKeyFactory getPublicKeyFactory(X509CertificateFactory x509CertificateFactory) {
        return new PublicKeyFactory(x509CertificateFactory);
    }

    @Provides
    @Singleton
    public MetadataCertificatesRepository getMetadataCertificateRepository(@Named(VERIFY_METADATA_RESOLVER) MetadataResolver metadataResolver, @Named("VerifyCertificateValidator") CertificateValidator certificateValidator) {
        return new MetadataCertificatesRepository(metadataResolver, certificateValidator, new CertificateExtractor());
    }

    @Provides
    @Singleton
    @Named("MatchingServiceClient")
    public JsonClient getMatchingServiceClient(Environment environment, MatchingServiceAdapterConfiguration configuration) {
        Client matchingServiceClient = new JerseyClientBuilder(environment).using(configuration.getMatchingServiceClientConfiguration()).build("MatchingServiceClient");
        return new JsonClient(new ErrorHandlingClient(matchingServiceClient), new JsonResponseProcessor(environment.getObjectMapper()));
    }

    @Provides
    @Singleton
    @Named("VerifyTrustStoreConfiguration")
    public TrustStoreConfiguration getHubTrustStoreConfiguration(MatchingServiceAdapterConfiguration configuration) {
        return configuration.getHubTrustStoreConfiguration();
    }

    @Provides
    @Singleton
    @Named("CountryTrustStore")
    public Optional<KeyStore> getCountryTrustStoreConfiguration(MatchingServiceAdapterConfiguration configuration) {
        return Optional.of(configuration.getHubTrustStoreConfiguration().getTrustStore());
    }

    @Provides
    @Singleton
    @Named("VerifyTrustStore")
    public KeyStore getVerifyKeyStore(@Named("VerifyTrustStoreConfiguration") TrustStoreConfiguration trustStoreConfiguration) {
        return trustStoreConfiguration.getTrustStore();
    }

    @Provides
    @Singleton
    @PublicEncryptionKeyConfiguration
    public DeserializablePublicKeyConfiguration getPublicEncryptionKeyConfiguration(MatchingServiceAdapterConfiguration configuration) {
        return configuration.getEncryptionKeys().get(0).getPublicKey();
    }

    @Provides
    @Singleton
    public CertificateStore getCertificateStore(MatchingServiceAdapterConfiguration configuration) {
        List<Certificate> publicSigningCertificates = configuration.getSigningKeys().stream()
            .map(KeyPairConfiguration::getPublicKey)
            .map(key -> cert(key.getName(), key.getCert(), Signing))
            .collect(Collectors.toList());

        List<Certificate> publicEncryptionCertificates = Stream.of(configuration.getEncryptionKeys().get(0).getPublicKey())
            .map(key -> cert(key.getName(), key.getCert(), Encryption))
            .collect(Collectors.toList());

        return new CertificateStore(
                publicEncryptionCertificates,
                publicSigningCertificates);
    }

    private Certificate cert(String keyName, String cert, Certificate.KeyUse keyUse) {
        return new Certificate(
                keyName,
                cert.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "").replace(" ", ""),
                keyUse);
    }

    @Provides
    @Singleton
    public Function<HealthCheckResponseFromMatchingService, Element> getHealthcheckResponseFromMatchingServiceToElementTransformer(
        Injector injector
    ) {
        return new MsaTransformersFactory().getHealthcheckResponseFromMatchingServiceToElementTransformer(
                injector.getInstance(EncryptionKeyStore.class),
                injector.getInstance(IdaKeyStore.class),
                injector.getInstance(EntityToEncryptForLocator.class),
                injector.getInstance(MatchingServiceAdapterConfiguration.class)
        );
    }

    @Provides
    @Singleton
    private Function<OutboundResponseFromMatchingService, Element> getOutboundResponseFromMatchingServiceToElementTransformer(
        Injector injector
    ) {
        return new MsaTransformersFactory().getOutboundResponseFromMatchingServiceToElementTransformer(
                injector.getInstance(EncryptionKeyStore.class),
                injector.getInstance(IdaKeyStore.class),
                injector.getInstance(EntityToEncryptForLocator.class),
                injector.getInstance(MatchingServiceAdapterConfiguration.class)
        );
    }

    @Provides
    @Singleton
    private Function<OutboundResponseFromUnknownUserCreationService, Element> getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
        Injector injector
    ) {
        return new MsaTransformersFactory().getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
                injector.getInstance(EncryptionKeyStore.class),
                injector.getInstance(IdaKeyStore.class),
                injector.getInstance(EntityToEncryptForLocator.class),
                injector.getInstance(MatchingServiceAdapterConfiguration.class)
        );
    }

    @Provides
    @Singleton
    private ElementToOpenSamlXMLObjectTransformer<AttributeQuery> getAttributeQueryTransformer() {
        return new CoreTransformersFactory().getElementToOpenSamlXmlObjectTransformer();
    }

    @Provides
    @Singleton
    private VerifyAttributeQueryToInboundMatchingServiceRequestTransformer getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
        @Named(VERIFY_METADATA_RESOLVER) MetadataResolver metadataResolver,
        IdaKeyStore keyStore,
        MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
        @Named("VerifyCertificateChainEvaluableCriterion") CertificateChainEvaluableCriterion certificateChainEvaluableCriterion,
        @Named("HubEntityId") String hubEntityId) throws ComponentInitializationException {
        return new MsaTransformersFactory().getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
                metadataResolver,
                keyStore,
                matchingServiceAdapterConfiguration,
                hubEntityId,
                certificateChainEvaluableCriterion);
    }

    @Provides
    @Singleton
    private Function<AttributeQuery, InboundMatchingServiceRequest> getAttributeQueryToInboundMatchingServiceRequestTransformer(
            @Named(COUNTRY_METADATA_RESOLVER) Optional<MetadataResolver> countryMetadataResolver,
            @Named(VERIFY_METADATA_RESOLVER) MetadataResolver verifyMetadataResolver,
            @Named("VerifyCertificateValidator") CertificateValidator verifyCertificateValidator,
            @Named("CountryCertificateValidator") Optional<CertificateValidator> countryCertificateValidator,
            X509CertificateFactory x509CertificateFactory,
            IdaKeyStore eidasKeystore,
            VerifyAttributeQueryToInboundMatchingServiceRequestTransformer verifyTransformer) throws ComponentInitializationException {

        if (countryMetadataResolver.isPresent()) {
            EidasAttributesBasedAttributeQueryDiscriminator discriminator = new EidasAttributesBasedAttributeQueryDiscriminator(
                new AssertionDecrypter(new IdaKeyStoreCredentialRetriever(eidasKeystore), new EncryptionAlgorithmValidator(), new DecrypterFactory()),
                new ResolverBackedMetadataRepository(countryMetadataResolver.get())
            );

            EidasAttributeQueryToInboundMatchingServiceRequestTransformer eidasTransformer =
                    new MsaTransformersFactory().getEidasAttributeQueryToInboundMatchingServiceRequestTransformer(
                        new EidasAttributeQueryValidator(
                            verifyMetadataResolver,
                            countryMetadataResolver.get(),
                            verifyCertificateValidator,
                            countryCertificateValidator.get(),
                            new CertificateExtractor(),
                            x509CertificateFactory,
                            new DateTimeComparator(Duration.ZERO),
                            new AssertionDecrypter(new IdaKeyStoreCredentialRetriever(eidasKeystore), new EncryptionAlgorithmValidator(), new DecrypterFactory())
                        )
                    );

            return new DiscriminatingAttributeQueryToInboundMSRequestTransformer(discriminator, verifyTransformer, eidasTransformer);
        }

        return verifyTransformer;
    }

    @Provides
    @Singleton
    private ElementToOpenSamlXMLObjectTransformer<EntityDescriptor> getMetadataForSpTransformer() {
        return new CoreTransformersFactory().getElementToOpenSamlXmlObjectTransformer();
    }

    @Provides
    @Singleton
    private KeyDescriptorsUnmarshaller getCertificatesToKeyDescriptorsTransformer() {
        return new CoreTransformersFactory().getCertificatesToKeyDescriptorsTransformer();
    }

    @Provides
    @Singleton
    private Function<EntitiesDescriptor, Element> getEntityDescriptorToElementTransformer() {
        return new CoreTransformersFactory().getXmlObjectToElementTransformer();
    }

    @Provides
    @Singleton
    public IdaKeyStore getIdaKeyStore(MatchingServiceAdapterConfiguration configuration) {
        List<KeyPair> encryptionKeyPairs = configuration.getEncryptionKeys().stream()
            .map(pair -> new KeyPair(pair.getPublicKey().getPublicKey(), pair.getPrivateKey().getPrivateKey()))
            .collect(Collectors.toList());

        KeyPair signingKeyPair = new KeyPair(
            configuration.getSigningKeys().get(0).getPublicKey().getPublicKey(),
            configuration.getSigningKeys().get(0).getPrivateKey().getPrivateKey()
        );

        return new IdaKeyStore(signingKeyPair, encryptionKeyPairs);
    }

    @Provides
    @Singleton
    public MetadataConfiguration metadataConfiguration(MatchingServiceAdapterConfiguration msaConfiguration) {
        return msaConfiguration.getMetadataConfiguration();
    }

    @Provides
    @Singleton
    public MatchingServiceAttributeQueryHandler matchingServiceAttributeQueryHandler(
        AdapterToMatchingServiceProxy proxy,
        InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper inboundMapper,
        MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper outboundMapper) {
        return new MatchingServiceAttributeQueryHandler(proxy, inboundMapper, outboundMapper);
    }

    @Provides
    @Singleton
    @Named(VERIFY_METADATA_RESOLVER)
    private MetadataResolver getVerifyMetadataResolver(Environment environment, MatchingServiceAdapterConfiguration configuration) {
        MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, configuration.getMetadataConfiguration());
        environment.healthChecks().register("VerifyMetadataHealthCheck", new MetadataHealthCheck(metadataResolver, configuration.getMetadataConfiguration().getExpectedEntityId()));
        return metadataResolver;
    }

    @Provides
    @Singleton
    @Named(COUNTRY_METADATA_RESOLVER)
    private Optional<MetadataResolver> getCountryMetadataResolver(Environment environment, MatchingServiceAdapterConfiguration configuration) {
        if (configuration.getCountry() != null) {
            MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, configuration.getCountry().getMetadata());
            environment.healthChecks().register("CountryMetadataHealthCheck", new MetadataHealthCheck(metadataResolver, configuration.getCountry().getMetadata().getExpectedEntityId()));
            return Optional.of(metadataResolver);
        }
        return Optional.empty();
    }

}
