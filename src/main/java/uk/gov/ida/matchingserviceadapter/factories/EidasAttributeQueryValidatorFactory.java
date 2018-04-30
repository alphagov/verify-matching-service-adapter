package uk.gov.ida.matchingserviceadapter.factories;

import org.joda.time.Duration;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator;
import uk.gov.ida.matchingserviceadapter.validators.exceptions.SamlResponseValidationException;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.validators.Validator;

public class EidasAttributeQueryValidatorFactory {
    private final SignatureValidator verifySignatureValidator;
    private final MatchingServiceAdapterConfiguration configuration;
    private final AssertionDecrypter assertionDecrypter;
    private final HubAssertionExtractor hubAssertionExtractor;
    private final MetadataResolverRepository eidasMetadataResolverRepository;

    public EidasAttributeQueryValidatorFactory(SignatureValidator verifySignatureValidator,
                                               MatchingServiceAdapterConfiguration configuration,
                                               AssertionDecrypter assertionDecrypter,
                                               HubAssertionExtractor hubAssertionExtractor,
                                               MetadataResolverRepository eidasMetadataResolverRepository) {

        this.verifySignatureValidator = verifySignatureValidator;
        this.configuration = configuration;
        this.assertionDecrypter = assertionDecrypter;
        this.hubAssertionExtractor = hubAssertionExtractor;
        this.eidasMetadataResolverRepository = eidasMetadataResolverRepository;
    }

    public Validator<AttributeQuery> build(String issuerEntityId) {
        return new EidasAttributeQueryValidator(
                verifySignatureValidator,
                createCountrySignatureValidator(issuerEntityId),
                new DateTimeComparator(Duration.standardSeconds(configuration.getClockSkew())),
                assertionDecrypter,
                hubAssertionExtractor,
                configuration.getEuropeanIdentity().getHubConnectorEntityId()
        );
    }

    private SignatureValidator createCountrySignatureValidator(String issuerEntityId) {
        return eidasMetadataResolverRepository.getSignatureTrustEngine(issuerEntityId)
                    .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
                    .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + issuerEntityId));
    }
}
