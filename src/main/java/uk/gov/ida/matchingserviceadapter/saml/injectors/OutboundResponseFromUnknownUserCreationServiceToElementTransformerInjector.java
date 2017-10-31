package uk.gov.ida.matchingserviceadapter.saml.injectors;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OutboundResponseFromUnknownUserCreationServiceToElementTransformerInjector implements Function<OutboundResponseFromUnknownUserCreationService, Element> {

    private Function<OutboundResponseFromUnknownUserCreationService, Element> outboundResponseFromUnknownUserCreationServiceToElementTransformer;

    @Inject
    public OutboundResponseFromUnknownUserCreationServiceToElementTransformerInjector(EncryptionKeyStore encryptionKeyStore,
                                                                             IdaKeyStore idaKeyStore,
                                                                             EntityToEncryptForLocator entityToEncryptForLocator,
                                                                             MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        outboundResponseFromUnknownUserCreationServiceToElementTransformer = new MsaTransformersFactory().getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(encryptionKeyStore,
                idaKeyStore,
                entityToEncryptForLocator,
                matchingServiceAdapterConfiguration);
    }

    @Override
    public Element apply(OutboundResponseFromUnknownUserCreationService outboundResponseFromUnknownUserCreationService) {
        return outboundResponseFromUnknownUserCreationServiceToElementTransformer.apply(outboundResponseFromUnknownUserCreationService);
    }
}
