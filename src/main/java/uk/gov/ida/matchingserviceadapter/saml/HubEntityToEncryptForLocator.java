package uk.gov.ida.matchingserviceadapter.saml;

import uk.gov.ida.saml.security.EntityToEncryptForLocator;

import javax.inject.Inject;
import javax.inject.Named;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.HUB_ENTITY_ID;

public class HubEntityToEncryptForLocator implements EntityToEncryptForLocator {

    private final String hubEntityId;

    @Inject
    public HubEntityToEncryptForLocator(@Named(HUB_ENTITY_ID) String hubEntityId) {
        this.hubEntityId = hubEntityId;
    }

    @Override
    public String fromRequestId(String requestId) {
        return hubEntityId;
    }
}
