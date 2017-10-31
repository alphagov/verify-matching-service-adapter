package uk.gov.ida.matchingserviceadapter.saml.injectors;

import org.glassfish.hk2.api.Factory;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.VerifyAttributeQueryToInboundMatchingServiceRequestTransformer;
import uk.gov.ida.saml.security.CertificateChainEvaluableCriterion;
import uk.gov.ida.saml.security.IdaKeyStore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterPKIBinder.VERIFY_CERTIFICATE_CHAIN_EVALUABLE_CRITERION;
import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.HUB_ENTITY_ID;

@Singleton
public class VerifyAttributeQueryToInboundMatchingServiceRequestTransformerFactory implements Factory<VerifyAttributeQueryToInboundMatchingServiceRequestTransformer> {

    private final MetadataResolver metadataResolver;
    private final IdaKeyStore keyStore;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final CertificateChainEvaluableCriterion certificateChainEvaluableCriterion;
    private final String hubEntityId;

    @Inject
    public VerifyAttributeQueryToInboundMatchingServiceRequestTransformerFactory(MetadataResolver metadataResolver,
                                                                                 IdaKeyStore keyStore,
                                                                                 MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
                                                                                 @Named(VERIFY_CERTIFICATE_CHAIN_EVALUABLE_CRITERION) CertificateChainEvaluableCriterion certificateChainEvaluableCriterion,
                                                                                 @Named(HUB_ENTITY_ID) String hubEntityId) {

        this.metadataResolver = metadataResolver;
        this.keyStore = keyStore;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.certificateChainEvaluableCriterion = certificateChainEvaluableCriterion;
        this.hubEntityId = hubEntityId;
    }


    @Override
    public VerifyAttributeQueryToInboundMatchingServiceRequestTransformer provide() {
        return new MsaTransformersFactory().getVerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
                metadataResolver,
                keyStore,
                matchingServiceAdapterConfiguration,
                hubEntityId,
                certificateChainEvaluableCriterion);
    }

    @Override
    public void dispose(VerifyAttributeQueryToInboundMatchingServiceRequestTransformer instance) {
        // do nothing
    }

}
