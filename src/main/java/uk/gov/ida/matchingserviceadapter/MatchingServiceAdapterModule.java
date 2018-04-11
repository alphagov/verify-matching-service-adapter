package uk.gov.ida.matchingserviceadapter;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
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
import uk.gov.ida.matchingserviceadapter.controllogic.MatchingServiceLocator;
import uk.gov.ida.matchingserviceadapter.controllogic.ServiceLocator;
import uk.gov.ida.matchingserviceadapter.controllogic.UnknownUserAttributeQueryHandler;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionResponseFactory;
import uk.gov.ida.matchingserviceadapter.factories.EidasAttributeQueryValidatorFactory;
import uk.gov.ida.matchingserviceadapter.mappers.DocumentToInboundMatchingServiceRequestMapper;
import uk.gov.ida.matchingserviceadapter.mappers.InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingDatasetToMatchingDatasetDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxyImpl;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;
import uk.gov.ida.matchingserviceadapter.resources.DelegatingMatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.HealthCheckResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.resources.VerifyMatchingServiceResponseGenerator;
import uk.gov.ida.matchingserviceadapter.rest.configuration.verification.FixedCertificateChainValidator;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributesBasedAttributeQueryDiscriminator;
import uk.gov.ida.matchingserviceadapter.services.DelegatingMatchingService;
import uk.gov.ida.matchingserviceadapter.services.EidasMatchingService;
import uk.gov.ida.matchingserviceadapter.services.HealthCheckMatchingService;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;
import uk.gov.ida.matchingserviceadapter.services.VerifyMatchingService;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.metadata.EidasTrustAnchorHealthCheck;
import uk.gov.ida.saml.metadata.EidasTrustAnchorResolver;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;
import uk.gov.ida.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import uk.gov.ida.saml.metadata.transformers.KeyDescriptorsUnmarshaller;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.CertificateChainEvaluableCriterion;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedEncryptionCredentialResolver;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.truststore.KeyStoreLoader;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Encryption;
import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Signing;
class MatchingServiceAdapterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PKIXParametersProvider.class).toInstance(new PKIXParametersProvider());
        bind(SoapMessageManager.class).toInstance(new SoapMessageManager());
        bind(X509CertificateFactory.class);
        bind(KeyStoreLoader.class);
        bind(IdGenerator.class);
        bind(ExpiredCertificateMetadataFilter.class);
        bind(ExceptionResponseFactory.class);
        bind(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class);
        bind(MatchingServiceAdapterMetadataRepository.class);
        bind(DocumentToInboundMatchingServiceRequestMapper.class);
        bind(IdGenerator.class);
        bind(MatchingServiceAssertionFactory.class);
        bind(UserAccountCreationAttributeExtractor.class);
        bind(UnknownUserAttributeQueryHandler.class);

        bind(PublicKeyInputStreamFactory.class).to(PublicKeyFileInputStreamFactory.class).in(Singleton.class);
        bind(AssertionLifetimeConfiguration.class).to(MatchingServiceAdapterConfiguration.class).in(Singleton.class);
        bind(MatchingServiceProxy.class).to(MatchingServiceProxyImpl.class).in(Singleton.class);
        bind(ManifestReader.class).toInstance(new ManifestReader());
        bind(MatchingDatasetToMatchingDatasetDtoMapper.class).toInstance(new MatchingDatasetToMatchingDatasetDtoMapper());
    }

    @Provides
    @Singleton
    private MetadataBackedEncryptionCredentialResolver hubEncryptionCredentialResolver(MetadataCredentialResolver metadataCredentialResolver) {
        return new MetadataBackedEncryptionCredentialResolver(metadataCredentialResolver, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Provides
    @Singleton
    public IdaKeyStoreCredentialRetriever getIdaKeyStoreCredentialRetriever(IdaKeyStore keyStore){
        return new IdaKeyStoreCredentialRetriever(keyStore);
    }

    @Provides
    @Singleton
    private UserIdHashFactory getUserIdHashFactory(MatchingServiceAdapterConfiguration configuration) {
        return new UserIdHashFactory(configuration.getEntityId());
    }

    @Provides
    @Singleton
    private InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper getInboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(
            UserIdHashFactory userIdHashFactory,
            MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper,
            MatchingServiceAdapterConfiguration configuration) {
        return new InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(userIdHashFactory, matchingDatasetToMatchingDatasetDtoMapper, configuration.isEidasEnabled());
    }

    @Provides
    @Singleton
    public MatchingService getMatchingService(ServiceLocator<MatchingServiceRequestContext, MatchingService> matchingServiceLocator, SoapMessageManager soapMessageManager, ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller, AssertionDecrypter assertionDecrypter) {
        return new DelegatingMatchingService(matchingServiceLocator, soapMessageManager, attributeQueryUnmarshaller, assertionDecrypter);
    }

    @Provides
    @Singleton
    public MatchingServiceResponseGenerator<MatchingServiceResponse> getResponseGenerator(SoapMessageManager soapMessageManager,
                                                                                          Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer,
                                                                                          ManifestReader manifestReader,
                                                                                          Function<OutboundResponseFromMatchingService, Element> responseElementTransformer
    ) {
        return new DelegatingMatchingServiceResponseGenerator(
            ImmutableMap.of(
                HealthCheckMatchingServiceResponse.class,
                new HealthCheckResponseGenerator(soapMessageManager, healthCheckResponseTransformer, manifestReader),
                VerifyMatchingServiceResponse.class,
                new VerifyMatchingServiceResponseGenerator(soapMessageManager, responseElementTransformer)
            ));
    }

    @Provides
    @Singleton
    public ServiceLocator<MatchingServiceRequestContext, MatchingService> getServiceLocator(
        HealthCheckMatchingService healthCheckMatchingService,
        VerifyMatchingService verifyMatchingService,
        Optional<EidasMatchingService> eidasMatchingService,
        Optional<EidasMetadataResolverRepository> eidasMetadataResolverRepository
    ) {
        Map<Predicate<MatchingServiceRequestContext>, MatchingService> servicesMap = new LinkedHashMap<>();
        servicesMap.put((MatchingServiceRequestContext ctx) -> ctx.getAssertions().isEmpty(), healthCheckMatchingService);
        eidasMetadataResolverRepository.ifPresent(countryResolver -> servicesMap.put(new EidasAttributesBasedAttributeQueryDiscriminator(eidasMetadataResolverRepository.get()), eidasMatchingService.get()));
        servicesMap.put(ctx -> true, verifyMatchingService);

        return new MatchingServiceLocator(
            ImmutableMap.copyOf(servicesMap)
        );
    }

    @Provides
    @Singleton
    public HealthCheckMatchingService getHealthCheckMatchingService(ManifestReader manifestReader, MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        return new HealthCheckMatchingService(manifestReader, matchingServiceAdapterConfiguration);
    }

    @Provides
    @Singleton
    public VerifyMatchingService getVerifyMatchingService(
        MatchingServiceAttributeQueryHandler attributeQueryHandler,
        DocumentToInboundMatchingServiceRequestMapper documentToInboundMatchingServiceRequestMapper
    ) {
        return new VerifyMatchingService(attributeQueryHandler, documentToInboundMatchingServiceRequestMapper);
    }

    @Provides
    @Singleton
    public Optional<EidasMatchingService> getEidasMatchingService(
            MetadataBackedSignatureValidator verifySignatureValidator,
            MatchingServiceAdapterConfiguration configuration,
            AssertionDecrypter assertionDecrypter,
            UserIdHashFactory userIdHashFactory,
            MatchingServiceProxy matchingServiceClient,
            @Named("HubEntityId") String hubEntityId,
            MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper,
            Optional<EidasMetadataResolverRepository> eidasMetadataResolverRepository) {

        return eidasMetadataResolverRepository.map(eidasMetadataResolverRepositoryValue ->
            new EidasMatchingService(
                new EidasAttributeQueryValidatorFactory(
                    verifySignatureValidator,
                    configuration,
                    assertionDecrypter,
                    eidasMetadataResolverRepositoryValue),
                new MsaTransformersFactory().getEidasMatchingRequestToMSRequestTransformer(userIdHashFactory, hubEntityId),
                matchingServiceClient,
                responseMapper));
    }

    @Provides
    @Singleton
    public MetadataBackedSignatureValidator verifyMetadataSignatureValidator(ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine, CertificateChainEvaluableCriterion certificateChainEvaluableCriterion) {
       return MetadataBackedSignatureValidator.withCertificateChainValidation(explicitKeySignatureTrustEngine, certificateChainEvaluableCriterion);
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
    @Named("VerifyFixedCertificateChainValidator")
    public FixedCertificateChainValidator getFixedChainCertificateValidator(
        @Named("VerifyTrustStore") KeyStore keyStore,
        CertificateChainValidator certificateChainValidator) {
        return new FixedCertificateChainValidator(keyStore, certificateChainValidator);
    }

    @Provides
    @Singleton
    public CertificateChainValidator getCertificateChainValidator(PKIXParametersProvider pkixParametersProvider, X509CertificateFactory x509CertificateFactory) {
        return new CertificateChainValidator(pkixParametersProvider, x509CertificateFactory);
    }

    @Provides
    @Singleton
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
        MetadataBackedEncryptionCredentialResolver encryptionCredentialResolver,
        IdaKeyStore idaKeyStore,
        EntityToEncryptForLocator entityToEncryptForLocator,
        MatchingServiceAdapterConfiguration configuration
    ) {
        return new MsaTransformersFactory().getHealthcheckResponseFromMatchingServiceToElementTransformer(
                encryptionCredentialResolver,
                idaKeyStore,
                entityToEncryptForLocator,
                configuration
        );
    }

    @Provides
    @Singleton
    private Function<OutboundResponseFromMatchingService, Element> getOutboundResponseFromMatchingServiceToElementTransformer(
        MetadataBackedEncryptionCredentialResolver encryptionCredentialResolver,
        IdaKeyStore idaKeyStore,
        EntityToEncryptForLocator entityToEncryptForLocator,
        MatchingServiceAdapterConfiguration configuration
    ) {
        return new MsaTransformersFactory().getOutboundResponseFromMatchingServiceToElementTransformer(
                encryptionCredentialResolver,
                idaKeyStore,
                entityToEncryptForLocator,
                configuration
        );
    }

    @Provides
    @Singleton
    private Function<OutboundResponseFromUnknownUserCreationService, Element> getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
        MetadataBackedEncryptionCredentialResolver encryptionCredentialResolver,
        IdaKeyStore idaKeyStore,
        EntityToEncryptForLocator entityToEncryptForLocator,
        MatchingServiceAdapterConfiguration configuration
    ) {
        return new MsaTransformersFactory().getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
            encryptionCredentialResolver,
            idaKeyStore,
            entityToEncryptForLocator,
            configuration
        );
    }

    @Provides
    @Singleton
    private ElementToOpenSamlXMLObjectTransformer<AttributeQuery> getAttributeQueryTransformer() {
        return new CoreTransformersFactory().getElementToOpenSamlXmlObjectTransformer();
    }

    @Provides
    @Singleton
    private Function<AttributeQuery, InboundVerifyMatchingServiceRequest> getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
            MetadataBackedSignatureValidator metadataBackedSignatureValidator,
            IdaKeyStore keyStore,
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            @Named("HubEntityId") String hubEntityId) {
        return new MsaTransformersFactory().getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
                metadataBackedSignatureValidator,
                keyStore,
                matchingServiceAdapterConfiguration,
                hubEntityId);
    }

    @Provides
    public AssertionDecrypter getAssertionDecrypter(IdaKeyStore eidasKeystore) {
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(eidasKeystore);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        return new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);
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
        MatchingServiceProxy proxy,
        InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper inboundMapper,
        MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper outboundMapper) {
        return new MatchingServiceAttributeQueryHandler(proxy, inboundMapper, outboundMapper);
    }

    @Provides
    @Singleton
    private Optional<EidasTrustAnchorResolver> getEidasTrustAnchorResolver(Environment environment, MatchingServiceAdapterConfiguration configuration) {
        if (configuration.isEidasEnabled()) {
            EidasMetadataConfiguration metadataConfiguration = configuration.getEuropeanIdentity().getAggregatedMetadata();

            Client client = new JerseyClientBuilder(environment)
                    .using(metadataConfiguration.getJerseyClientConfiguration())
                    .build(metadataConfiguration.getJerseyClientName());

            return Optional.of(new EidasTrustAnchorResolver(metadataConfiguration.getTrustAnchorUri(),
                    client,
                    metadataConfiguration.getTrustStore()));
        }

        return Optional.empty();
    }

    @Provides
    @Singleton
    private Optional<EidasMetadataResolverRepository> getEidasMetadataResolverRepository(Environment environment, MatchingServiceAdapterConfiguration configuration, Optional<EidasTrustAnchorResolver> trustAnchorResolver) {
        if (trustAnchorResolver.isPresent()) {
            EidasMetadataResolverRepository resolverRepository = new EidasMetadataResolverRepository(trustAnchorResolver.get(),
                    environment,
                    configuration.getEuropeanIdentity().getAggregatedMetadata(),
                    new DropwizardMetadataResolverFactory(),
                    new Timer(),
                    new MetadataSignatureTrustEngineFactory());
            environment.healthChecks().register("TrustAnchorHealthCheck", new EidasTrustAnchorHealthCheck(resolverRepository));
            return Optional.of(resolverRepository);
        }

        return Optional.empty();
    }
}
