package uk.gov.ida.matchingserviceadapter.factories;

import org.joda.time.Duration;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.validation.validators.Validator;

public class EidasAttributeQueryValidatorFactory {
    private final MetadataResolver verifyMetadataResolver;
    private final CertificateValidator verifyCertificateValidator;
    private final X509CertificateFactory x509CertificateFactory;
    private final MatchingServiceAdapterConfiguration configuration;
    private final AssertionDecrypter assertionDecrypter;
    private final EidasMetadataResolverRepository eidasMetadataResolverRepository;

    public EidasAttributeQueryValidatorFactory(MetadataResolver verifyMetadataResolver,
                                               CertificateValidator verifyCertificateValidator,
                                               X509CertificateFactory x509CertificateFactory,
                                               MatchingServiceAdapterConfiguration configuration,
                                               AssertionDecrypter assertionDecrypter,
                                               EidasMetadataResolverRepository eidasMetadataResolverRepository) {

        this.verifyMetadataResolver = verifyMetadataResolver;
        this.verifyCertificateValidator = verifyCertificateValidator;
        this.x509CertificateFactory = x509CertificateFactory;
        this.configuration = configuration;
        this.assertionDecrypter = assertionDecrypter;
        this.eidasMetadataResolverRepository = eidasMetadataResolverRepository;
    }

    public Validator<AttributeQuery> build(String issuerEntityId) {
        return new EidasAttributeQueryValidator(
                verifyMetadataResolver,
                eidasMetadataResolverRepository.getMetadataResolver(issuerEntityId),
                verifyCertificateValidator,
                null,
                new CertificateExtractor(),
                x509CertificateFactory,
                new DateTimeComparator(Duration.standardSeconds(configuration.getClockSkew())),
                assertionDecrypter,
                configuration.getEuropeanIdentity().getHubConnectorEntityId()
        );
    }
}
