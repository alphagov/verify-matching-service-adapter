package uk.gov.ida.matchingserviceadapter.saml.injectors;

import org.glassfish.hk2.api.Factory;
import org.joda.time.Duration;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.services.EidasMatchingService;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterMetadataBinder.COUNTRY_METADATA_RESOLVER;
import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterMetadataBinder.VERIFY_METADATA_RESOLVER;
import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterPKIBinder.COUNTRY_CERTIFICATE_VALIDATOR;
import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterPKIBinder.VERIFY_CERTIFICATE_VALIDATOR;
import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.HUB_ENTITY_ID;

@Singleton
public class EidasMatchingServiceFactory implements Factory<Optional<EidasMatchingService>> {

    private final Optional<EidasMatchingService> eidasMatchingService;

    @Inject
    public EidasMatchingServiceFactory(
            @Named(COUNTRY_METADATA_RESOLVER) Optional<MetadataResolver> countryMetadataResolver,
            @Named(VERIFY_METADATA_RESOLVER) MetadataResolver verifyMetadataResolver,
            @Named(VERIFY_CERTIFICATE_VALIDATOR) CertificateValidator verifyCertificateValidator,
            @Named(COUNTRY_CERTIFICATE_VALIDATOR) Optional<CertificateValidator> countryCertificateValidator,
            X509CertificateFactory x509CertificateFactory,
            MatchingServiceAdapterConfiguration configuration,
            AssertionDecrypter assertionDecrypter,
            UserIdHashFactory userIdHashFactory,
            MatchingServiceProxy matchingServiceClient,
            @Named(HUB_ENTITY_ID) String hubEntityId,
            MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper) {

        if (configuration.isEidasEnabled()) {
            eidasMatchingService = countryMetadataResolver.map(countryMetadataResolverValue ->
                    new EidasMatchingService(
                            new EidasAttributeQueryValidator(
                                    verifyMetadataResolver,
                                    countryMetadataResolverValue,
                                    verifyCertificateValidator,
                                    countryCertificateValidator.get(),
                                    new CertificateExtractor(),
                                    x509CertificateFactory,
                                    new DateTimeComparator(Duration.standardSeconds(configuration.getClockSkew())),
                                    assertionDecrypter,
                                    configuration.getEuropeanIdentity().getHubConnectorEntityId()
                            ),
                            new MsaTransformersFactory().getEidasMatchingRequestToMSRequestTransformer(userIdHashFactory, hubEntityId),
                            matchingServiceClient,
                            matchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper));
        } else {
            eidasMatchingService = Optional.empty();
        }
    }

    @Override
    public Optional<EidasMatchingService> provide() {
        return eidasMatchingService;
    }

    @Override
    public void dispose(Optional<EidasMatchingService> instance) {
        // do nothing
    }

}
