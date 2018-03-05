package uk.gov.ida.matchingserviceadapter.saml.api;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.log4j.Logger;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryAssertionsValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.InboundMatchingServiceRequestUnmarshaller;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.VerifyAttributeQueryToInboundMatchingServiceRequestTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.ResponseToElementTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.HealthCheckResponseFromMatchingServiceTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAssertionToAssertionTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.MatchingServiceAuthnStatementToAuthnStatementTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromMatchingServiceToSamlResponseTransformer;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers.OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer;
import uk.gov.ida.matchingserviceadapter.services.EidasMatchingRequestToMSRequestTransformer;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.core.domain.AddressFactory;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.IdentityProviderAssertionUnmarshaller;
import uk.gov.ida.saml.core.transformers.IdentityProviderAuthnStatementUnmarshaller;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
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
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.CertificateChainEvaluableCriterion;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EncrypterFactory;
import uk.gov.ida.saml.security.EncryptionCredentialFactory;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.saml.serializers.XmlObjectToElementTransformer;
import uk.gov.ida.validation.validators.Validator;

import java.util.function.Function;

@SuppressWarnings("unused")
public class MsaTransformersFactory {

    private static final Logger LOG = Logger.getLogger(MsaTransformersFactory.class);

    private CoreTransformersFactory coreTransformersFactory;

    public MsaTransformersFactory() {
        coreTransformersFactory = new CoreTransformersFactory();
    }

    public ResponseToElementTransformer getResponseToElementTransformer(
            EncryptionKeyStore encryptionKeyStore,
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
                new EncryptionCredentialFactory(encryptionKeyStore),
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
            final EncryptionKeyStore encryptionKeyStore,
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
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                configuration);
        return t2.compose(t1);
    }

    public Function<OutboundResponseFromMatchingService, Element> getOutboundResponseFromMatchingServiceToElementTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration) {
        Function<OutboundResponseFromMatchingService, Response> t1 = new OutboundResponseFromMatchingServiceToSamlResponseTransformer(
                new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                new OpenSamlXmlObjectFactory(),
                createMatchingServiceAssertionToAssertionTransformer()
        );
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionKeyStore,
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
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            MatchingServiceAdapterConfiguration configuration
    ){
        Function<HealthCheckResponseFromMatchingService, Response> t1 = getHealthCheckResponseFromMatchingServiceToResponseTransformer();
        Function<Response, Element> t2 = getResponseToElementTransformer(
                encryptionKeyStore,
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
            final MetadataResolver metaDataResolver,
            final IdaKeyStore keyStore,
            final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            final String hubEntityId,
            CertificateChainEvaluableCriterion certificateChainEvaluableCriterion) {
        HubAssertionUnmarshaller hubAssertionTransformer = coreTransformersFactory.getAssertionToHubAssertionTransformer(hubEntityId);
        IdentityProviderAssertionUnmarshaller identityProviderAssertionTransformer = new IdentityProviderAssertionUnmarshaller(
                new MatchingDatasetUnmarshaller(new AddressFactory()),
                new IdentityProviderAuthnStatementUnmarshaller(new AuthnContextFactory()),
                hubEntityId
        );
        SignatureValidator signatureValidator;
        try {
            signatureValidator = getMetadataBackedSignatureValidator(metaDataResolver, certificateChainEvaluableCriterion);
        } catch (ComponentInitializationException e) {
            LOG.info("Could not initialise metadata backed signature validator");
            throw new RuntimeException(e);
        }
        return new VerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
                new SamlAttributeQueryValidator(),
                new AttributeQuerySignatureValidator(new SamlMessageSignatureValidator(signatureValidator)),
                new SamlAssertionsSignatureValidator(new SamlMessageSignatureValidator(signatureValidator)),
                new InboundMatchingServiceRequestUnmarshaller(hubAssertionTransformer, identityProviderAssertionTransformer),
                new SamlAttributeQueryAssertionsValidator(getAssertionValidator(), getIdentityProviderAssertionValidator(), matchingServiceAdapterConfiguration, hubEntityId),
                new AssertionDecrypter(new IdaKeyStoreCredentialRetriever(keyStore), new EncryptionAlgorithmValidator(), new DecrypterFactory()),
                hubEntityId);
    }

    private SignatureValidator getMetadataBackedSignatureValidator(MetadataResolver metadataResolver, CertificateChainEvaluableCriterion certificateChainEvaluableCriterion) throws ComponentInitializationException {
        BasicRoleDescriptorResolver basicRoleDescriptorResolver = new BasicRoleDescriptorResolver(metadataResolver);
        basicRoleDescriptorResolver.initialize();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver();
        metadataCredentialResolver.setRoleDescriptorResolver(basicRoleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        metadataCredentialResolver.initialize();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(
                metadataCredentialResolver, DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver()
        );
        return MetadataBackedSignatureValidator.withCertificateChainValidation(explicitKeySignatureTrustEngine, certificateChainEvaluableCriterion);
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

    public Function<MatchingServiceRequestContext, MatchingServiceRequestDto> getEidasMatchingRequestToMSRequestTransformer(
        UserIdHashFactory userIdHashFactory, String hubEntityId) {
        return new EidasMatchingRequestToMSRequestTransformer(userIdHashFactory, hubEntityId, coreTransformersFactory.getAssertionToHubAssertionTransformer(hubEntityId));
    }
}
