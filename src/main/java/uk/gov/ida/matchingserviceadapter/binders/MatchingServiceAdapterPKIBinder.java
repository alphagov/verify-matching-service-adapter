package uk.gov.ida.matchingserviceadapter.binders;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFileInputStreamFactory;
import uk.gov.ida.common.shared.security.PublicKeyInputStreamFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.common.shared.security.verification.CertificateChainValidator;
import uk.gov.ida.common.shared.security.verification.PKIXParametersProvider;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.configuration.KeyPairConfiguration;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.rest.MetadataPublicKeyStore;
import uk.gov.ida.matchingserviceadapter.rest.configuration.verification.FixedCertificateChainValidator;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.CertificateChainEvaluableCriterion;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SigningKeyStore;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.truststore.KeyStoreLoader;

import javax.inject.Singleton;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Encryption;
import static uk.gov.ida.common.shared.security.Certificate.KeyUse.Signing;

public class MatchingServiceAdapterPKIBinder extends AbstractBinder {

    public static final String COUNTRY_KEY_STORE = "CountryKeyStore";
    public static final String VERIFY_KEY_STORE = "VerifyKeyStore";
    public static final String VERIFY_CERTIFICATE_VALIDATOR = "VerifyCertificateValidator";
    public static final String COUNTRY_CERTIFICATE_VALIDATOR = "CountryCertificateValidator";
    public static final String COUNTRY_FIXED_CERTIFICATE_CHAIN_VALIDATOR = "CountryFixedCertificateChainValidator";
    public static final String VERIFY_FIXED_CERTIFICATE_CHAIN_VALIDATOR = "VerifyFixedCertificateChainValidator";
    public static final String VERIFY_CERTIFICATE_CHAIN_EVALUABLE_CRITERION = "VerifyCertificateChainEvaluableCriterion";
    public static final String VERIFY_TRUST_STORE_CONFIGURATION = "VerifyTrustStoreConfiguration";

    private final MatchingServiceAdapterConfiguration configuration;

    public MatchingServiceAdapterPKIBinder(MatchingServiceAdapterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        final X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        final PKIXParametersProvider pkixParametersProvider = new PKIXParametersProvider();
        final CertificateChainValidator certificateChainValidator = new CertificateChainValidator(pkixParametersProvider, x509CertificateFactory);
        bind(pkixParametersProvider).to(PKIXParametersProvider.class);
        bind(x509CertificateFactory).to(X509CertificateFactory.class);
        bind(certificateChainValidator).to(CertificateChainValidator.class);
        bind(CertificateChainEvaluableCriterion.class).to(CertificateChainEvaluableCriterion.class);
        bind(CertificateExtractor.class).to(CertificateExtractor.class);

        // eidas certs
        Optional<KeyStore> countryKeyStore = Optional.empty();
        if (configuration.isEidasEnabled()) {
            countryKeyStore = Optional.of(configuration.getEuropeanIdentity().getMetadata().getTrustStore());
        }
        bind(countryKeyStore).named(COUNTRY_KEY_STORE).to(new TypeLiteral<Optional<KeyStore>>() {});
        final Optional<FixedCertificateChainValidator> countryFixedCertificateChainValidator = countryKeyStore.map(keyStore -> new FixedCertificateChainValidator(keyStore, certificateChainValidator));
        bind(countryFixedCertificateChainValidator).named(COUNTRY_FIXED_CERTIFICATE_CHAIN_VALIDATOR).to(new TypeLiteral<Optional<FixedCertificateChainValidator>>() {});
        bind(countryFixedCertificateChainValidator.map(ccV -> new CertificateValidator(x509CertificateFactory, ccV))).named(COUNTRY_CERTIFICATE_VALIDATOR).to(new TypeLiteral<Optional<CertificateValidator>>() {});

        // verify certs
        final KeyStore verifyKeyStore = configuration.getHubTrustStoreConfiguration().getTrustStore();
        bind(verifyKeyStore).named(VERIFY_KEY_STORE).to(KeyStore.class);
        final FixedCertificateChainValidator verifyFixedCertificateChainValidator = new FixedCertificateChainValidator(verifyKeyStore, certificateChainValidator);
        bind(verifyFixedCertificateChainValidator).named(VERIFY_FIXED_CERTIFICATE_CHAIN_VALIDATOR).to(FixedCertificateChainValidator.class);
        final CertificateValidator verifyCertificateValidator = new CertificateValidator(x509CertificateFactory, verifyFixedCertificateChainValidator);
        bind(verifyCertificateValidator).named(VERIFY_CERTIFICATE_VALIDATOR).to(CertificateValidator.class);
        bind(new CertificateChainEvaluableCriterion(certificateChainValidator, verifyKeyStore)).named(VERIFY_CERTIFICATE_CHAIN_EVALUABLE_CRITERION).to(CertificateChainEvaluableCriterion.class);

        // the rest of the Verify stuff
        bind(PublicKeyFactory.class).to(PublicKeyFactory.class);
        bind(KeyStoreLoader.class).to(KeyStoreLoader.class);
        bind(IdaKeyStoreCredentialRetriever.class).to(IdaKeyStoreCredentialRetriever.class);
        bind(MetadataPublicKeyStore.class).to(SigningKeyStore.class).in(Singleton.class);
        bind(MetadataPublicKeyStore.class).to(EncryptionKeyStore.class).in(Singleton.class);
        bind(PublicKeyFileInputStreamFactory.class).to(PublicKeyInputStreamFactory.class).in(Singleton.class);
        bind(configuration.getHubTrustStoreConfiguration()).named(VERIFY_TRUST_STORE_CONFIGURATION).to(TrustStoreConfiguration.class);
        // a @PublicEncryptionKeyConfiguration annotation was on this previously - not used?
        bind(configuration.getEncryptionKeys().get(0).getPublicKey()).to(DeserializablePublicKeyConfiguration.class);

        List<Certificate> publicSigningCertificates = configuration.getSigningKeys().stream()
                .map(KeyPairConfiguration::getPublicKey)
                .map(key -> cert(key.getName(), key.getCert(), Signing))
                .collect(Collectors.toList());
        List<Certificate> publicEncryptionCertificates = Stream.of(configuration.getEncryptionKeys().get(0).getPublicKey())
                .map(key -> cert(key.getName(), key.getCert(), Encryption))
                .collect(Collectors.toList());
        bind(new CertificateStore(publicEncryptionCertificates, publicSigningCertificates)).to(CertificateStore.class);

        List<KeyPair> encryptionKeyPairs = configuration.getEncryptionKeys().stream()
                .map(pair -> new KeyPair(pair.getPublicKey().getPublicKey(), pair.getPrivateKey().getPrivateKey()))
                .collect(Collectors.toList());
        KeyPair signingKeyPair = new KeyPair(
                configuration.getSigningKeys().get(0).getPublicKey().getPublicKey(),
                configuration.getSigningKeys().get(0).getPrivateKey().getPrivateKey()
        );
        final IdaKeyStore idaKeyStore = new IdaKeyStore(signingKeyPair, encryptionKeyPairs);
        bind(idaKeyStore).to(IdaKeyStore.class);
        bind(new AssertionDecrypter(new IdaKeyStoreCredentialRetriever(idaKeyStore), new EncryptionAlgorithmValidator(), new DecrypterFactory())).to(AssertionDecrypter.class);

    }

    private Certificate cert(String keyName, String cert, Certificate.KeyUse keyUse) {
        return new Certificate(
                keyName,
                cert.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "").replace(" ", ""),
                keyUse);
    }

}
