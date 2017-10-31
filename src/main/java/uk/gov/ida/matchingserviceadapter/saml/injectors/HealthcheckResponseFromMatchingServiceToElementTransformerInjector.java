package uk.gov.ida.matchingserviceadapter.saml.injectors;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.saml.security.EncryptionKeyStore;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class HealthcheckResponseFromMatchingServiceToElementTransformerInjector implements Function<HealthCheckResponseFromMatchingService, Element> {

    private Function<HealthCheckResponseFromMatchingService, Element> healthcheckResponseFromMatchingServiceToElementTransformer;

    @Inject
    public HealthcheckResponseFromMatchingServiceToElementTransformerInjector(EncryptionKeyStore encryptionKeyStore,
                                                                              IdaKeyStore idaKeyStore,
                                                                              EntityToEncryptForLocator entityToEncryptForLocator,
                                                                              MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        healthcheckResponseFromMatchingServiceToElementTransformer = new MsaTransformersFactory().getHealthcheckResponseFromMatchingServiceToElementTransformer(encryptionKeyStore,
                idaKeyStore,
                entityToEncryptForLocator,
                matchingServiceAdapterConfiguration);
    }

    @Override
    public Element apply(HealthCheckResponseFromMatchingService healthCheckResponseFromMatchingService) {
        return healthcheckResponseFromMatchingServiceToElementTransformer.apply(healthCheckResponseFromMatchingService);
    }
}
