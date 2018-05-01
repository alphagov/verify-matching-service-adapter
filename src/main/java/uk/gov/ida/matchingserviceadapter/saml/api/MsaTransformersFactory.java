package uk.gov.ida.matchingserviceadapter.saml.api;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryAssertionsValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.InboundMatchingServiceRequestUnmarshaller;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.VerifyAttributeQueryToInboundMatchingServiceRequestTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.ResponseToElementTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.HealthCheckResponseFromMatchingServiceTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAssertionToAssertionTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAuthnStatementToAuthnStatementTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromMatchingServiceToSamlResponseTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.core.domain.AddressFactory;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.CountryMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.IdentityProviderAssertionUnmarshaller;
import uk.gov.ida.saml.core.transformers.IdentityProviderAuthnStatementUnmarshaller;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.HubAssertionUnmarshaller;
import uk.gov.ida.saml.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import uk.gov.ida.saml.core.transformers.outbound.decorators.ResponseAssertionSigner;
import uk.gov.ida.saml.core.transformers.outbound.decorators.ResponseSignatureCreator;
import uk.gov.ida.saml.core.transformers.outbound.decorators.SamlResponseAssertionEncrypter;
import uk.gov.ida.saml.core.transformers.outbound.decorators.SamlSignatureSigner;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.core.validators.assertion.AssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;
import uk.gov.ida.saml.core.validators.subject.AssertionSubjectValidator;
import uk.gov.ida.saml.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import uk.gov.ida.saml.core.validators.subjectconfirmation.BasicAssertionSubjectConfirmationValidator;
import uk.gov.ida.saml.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller;
import uk.gov.ida.saml.hub.transformers.outbound.UnknownUserCreationIdaStatusMarshaller;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EncrypterFactory;
import uk.gov.ida.saml.security.EncryptionCredentialResolver;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.saml.serializers.XmlObjectToElementTransformer;

import java.util.function.Function;

@SuppressWarnings("unused")
public class MsaTransformersFactory {
    private CoreTransformersFactory coreTransformersFactory;

    public MsaTransformersFactory() {
        coreTransformersFactory = new CoreTransformersFactory();
    }

    public ResponseToElementTransformer getResponseToElementTransformer(
            EncryptionCredentialResolver encryptionCredentialResolver,
            IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEnryptForLocator,
            MatchingServiceAdapterConfiguration configuration
    ) {
        SignatureFactory signatureFactory = new SignatureFactory(
            new IdaKeyStoreCredentialRetriever(keyStore),
            new SignatureRSASHA1(),
            new DigestSHA256()
        );
        SamlResponseAssertionEncrypter assertionEncrypter = new SamlResponseAssertionEncrypter(
                encryptionCredentialResolver,
                new EncrypterFactory(),
                entityToEnryptForLocator);
        return new ResponseToElementTransformer(
                new XmlObjectToElementTransformer<>(),
                new SamlSignatureSigner<>(),
                assertionEncrypter,
                new ResponseAssertionSigner(signatureFactory),
                new ResponseSignatureCreator(signatureFactory)
        );
    }

    public HealthCheckResponseFromMatchingServiceTransformer getHealthCheckResponseFromMatchingServiceToResponseTransformer() {
        return new HealthCheckResponseFromMatchingServiceTransformer(
                new OpenSamlXmlObjectFactory(),
                new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory())
        );
    }

    public Function<OutboundResponseFromUnknownUserCreationService, Element> getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration) {
        Function<OutboundResponseFromUnknownUserCreationService, Response> t1 =
                new OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer(
                        new OpenSamlXmlObjectFactory(),
                        new UnknownUserCreationIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                        createMatchingServiceAssertionToAssertionTransformer()
                );
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
                entityToEncryptForLocator,
                configuration);
        return t2.compose(t1);
    }

    public Function<OutboundResponseFromMatchingService, Element> getOutboundResponseFromMatchingServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration) {
        Function<OutboundResponseFromMatchingService, Response> t1 = new OutboundResponseFromMatchingServiceToSamlResponseTransformer(
                new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                new OpenSamlXmlObjectFactory(),
                createMatchingServiceAssertionToAssertionTransformer()
        );
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
            entityToEncryptForLocator,
            configuration);
        return t2.compose(t1);
    }

    private MatchingServiceAssertionToAssertionTransformer createMatchingServiceAssertionToAssertionTransformer() {
        return new MatchingServiceAssertionToAssertionTransformer(
                new OpenSamlXmlObjectFactory(),
                new MatchingServiceAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory()),
                new OutboundAssertionToSubjectTransformer(new OpenSamlXmlObjectFactory())
        );
    }

    public Function<HealthCheckResponseFromMatchingService, Element> getHealthcheckResponseFromMatchingServiceToElementTransformer(
            final EncryptionCredentialResolver encryptionCredentialResolver,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration
    ){
        Function<HealthCheckResponseFromMatchingService, Response> t1 = getHealthCheckResponseFromMatchingServiceToResponseTransformer();
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionCredentialResolver,
                keyStore,
                entityToEncryptForLocator,
                configuration);
        return t2.compose(t1);
    }

    public Function<MatchingServiceAssertion, String> getMatchingServiceAssertionToStringTransformer() {
        Function<MatchingServiceAssertion, Assertion> matchingServiceAssertionToAssertionTransformer = createMatchingServiceAssertionToAssertionTransformer();
        Function<Assertion, String> assertionToStringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return assertionToStringTransformer.compose(matchingServiceAssertionToAssertionTransformer);
    }

    public VerifyAttributeQueryToInboundMatchingServiceRequestTransformer getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
            final MetadataBackedSignatureValidator signatureValidator,
            final IdaKeyStore keyStore,
            final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            final MetadataResolverRepository eidasMetadataResolverRepository,
            final String hubEntityId) {
        HubAssertionUnmarshaller hubAssertionTransformer = coreTransformersFactory.getAssertionToHubAssertionTransformer(hubEntityId);
        AddressFactory addressFactory = new AddressFactory();
        IdentityProviderAssertionUnmarshaller identityProviderAssertionTransformer = new IdentityProviderAssertionUnmarshaller(
                new VerifyMatchingDatasetUnmarshaller(addressFactory),
                new CountryMatchingDatasetUnmarshaller(addressFactory),
                new IdentityProviderAuthnStatementUnmarshaller(new AuthnContextFactory()),
                hubEntityId
        );
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        return new VerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
                new SamlAttributeQueryValidator(),
                new AttributeQuerySignatureValidator(new SamlMessageSignatureValidator(signatureValidator)),
                new SamlAssertionsSignatureValidator(new SamlMessageSignatureValidator(signatureValidator)),
                new InboundMatchingServiceRequestUnmarshaller(hubAssertionTransformer, identityProviderAssertionTransformer, eidasMetadataResolverRepository),
                new SamlAttributeQueryAssertionsValidator(getAssertionValidator(), getIdentityProviderAssertionValidator(), matchingServiceAdapterConfiguration, hubEntityId),
            new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter),
                hubEntityId);
    }

    private IdentityProviderAssertionValidator getIdentityProviderAssertionValidator() {
        return new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator()
        );
    }

    private AssertionValidator getAssertionValidator() {
        return new AssertionValidator(
                    new IssuerValidator(),
                    new AssertionSubjectValidator(),
                    new AssertionAttributeStatementValidator(),
                    new BasicAssertionSubjectConfirmationValidator()
        );
    }
}
