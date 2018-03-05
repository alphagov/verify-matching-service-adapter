package uk.gov.ida.matchingserviceadapter.saml.injectors;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OutboundResponseFromMatchingServiceToElementTransformerInjector implements Function<OutboundResponseFromMatchingService, Element> {

    private Function<OutboundResponseFromMatchingService, Element> outboundResponseFromMatchingServiceToElementTransformer;

    @Inject
    public OutboundResponseFromMatchingServiceToElementTransformerInjector(EncryptionKeyStore encryptionKeyStore,
                                                                           IdaKeyStore idaKeyStore,
                                                                           EntityToEncryptForLocator entityToEncryptForLocator,
                                                                           MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        outboundResponseFromMatchingServiceToElementTransformer = new MsaTransformersFactory().getOutboundResponseFromMatchingServiceToElementTransformer(encryptionKeyStore,
                idaKeyStore,
                entityToEncryptForLocator,
                matchingServiceAdapterConfiguration);
    }

    @Override
    public Element apply(OutboundResponseFromMatchingService outboundResponseFromMatchingService) {
        return outboundResponseFromMatchingServiceToElementTransformer.apply(outboundResponseFromMatchingService);
    }
}
