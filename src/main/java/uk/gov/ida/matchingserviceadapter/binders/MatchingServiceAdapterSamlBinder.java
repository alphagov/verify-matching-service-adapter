package uk.gov.ida.matchingserviceadapter.binders;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.HubEntityToEncryptForLocator;
import uk.gov.ida.matchingserviceadapter.saml.injectors.HealthcheckResponseFromMatchingServiceToElementTransformerInjector;
import uk.gov.ida.matchingserviceadapter.saml.injectors.OutboundResponseFromMatchingServiceToElementTransformerInjector;
import uk.gov.ida.matchingserviceadapter.saml.injectors.OutboundResponseFromUnknownUserCreationServiceToElementTransformerInjector;
import uk.gov.ida.matchingserviceadapter.saml.injectors.VerifyAttributeQueryToInboundMatchingServiceRequestTransformerFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;
import uk.gov.ida.saml.metadata.transformers.KeyDescriptorsUnmarshaller;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;

import java.util.Optional;
import java.util.function.Function;

public class MatchingServiceAdapterSamlBinder extends AbstractBinder {

    public static final String HUB_ENTITY_ID = "HubEntityId";
    public static final String MSA_ENTITY_ID = "MsaEntityId";
    public static final String HUB_CONNECTOR_ENTITY_ID = "HubConnectorEntityId";

    private final MatchingServiceAdapterConfiguration configuration;

    public MatchingServiceAdapterSamlBinder(MatchingServiceAdapterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(configuration).to(AssertionLifetimeConfiguration.class);

        bind(configuration.getHubEntityId()).named(HUB_ENTITY_ID).to(String.class);
        bind(configuration.getEntityId()).named(MSA_ENTITY_ID).to(String.class);
        final Optional<String> hubConnectorEntityId;
        if(configuration.isEidasEnabled()) {
            hubConnectorEntityId = Optional.ofNullable(configuration.getEuropeanIdentity().getHubConnectorEntityId());
        } else {
            hubConnectorEntityId = Optional.empty();
        }
        bind(hubConnectorEntityId).named(HUB_CONNECTOR_ENTITY_ID).to(new TypeLiteral<Optional<String>>() {});
        bind(HubEntityToEncryptForLocator.class).to(EntityToEncryptForLocator.class);
        bind(new OpenSamlXmlObjectFactory()).to(OpenSamlXmlObjectFactory.class);

        final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
        bind(coreTransformersFactory.getElementToOpenSamlXmlObjectTransformer()).to(new TypeLiteral<ElementToOpenSamlXMLObjectTransformer<EntityDescriptor>>() {});
        bind(coreTransformersFactory.getElementToOpenSamlXmlObjectTransformer()).to(new TypeLiteral<ElementToOpenSamlXMLObjectTransformer<AttributeQuery>>() {});
        bind(coreTransformersFactory.getCertificatesToKeyDescriptorsTransformer()).to(KeyDescriptorsUnmarshaller.class);
        bind(coreTransformersFactory.getXmlObjectToElementTransformer()).to(new TypeLiteral<Function<EntitiesDescriptor, Element>>() {});

        bind(HealthcheckResponseFromMatchingServiceToElementTransformerInjector.class).to(new TypeLiteral<Function<HealthCheckResponseFromMatchingService, Element>>() {});
        bind(OutboundResponseFromMatchingServiceToElementTransformerInjector.class).to(new TypeLiteral<Function<OutboundResponseFromMatchingService, Element>>() {});
        bind(OutboundResponseFromUnknownUserCreationServiceToElementTransformerInjector.class).to(new TypeLiteral<Function<OutboundResponseFromUnknownUserCreationService, Element>>() {});
        bindFactory(VerifyAttributeQueryToInboundMatchingServiceRequestTransformerFactory.class).to(new TypeLiteral<Function<AttributeQuery, InboundMatchingServiceRequest>>() {});
    }

}
