package uk.gov.ida.matchingserviceadapter.logging;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.slf4j.MDC;

public class MdcHelper {

    private MdcHelper() {}

    public static void addContextToMdc(AttributeQuery attributeQuery) {
        String messageId = attributeQuery.getID();
        String entityID = attributeQuery.getIssuer().getValue();

        MDC.put("messageId", messageId);
        MDC.put("entityId", entityID);
        MDC.put("logPrefix", "[AttributeQuery " + messageId + " from " + entityID + "] ");
        
    }
}
