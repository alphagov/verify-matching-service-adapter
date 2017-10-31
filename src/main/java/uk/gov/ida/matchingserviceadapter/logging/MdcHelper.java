package uk.gov.ida.matchingserviceadapter.logging;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.slf4j.MDC;

public class MdcHelper {

    private MdcHelper() {}

    public static void addContextToMdc(AttributeQuery attributeQuery) {
        addContextToMdc("AttributeQuery", attributeQuery.getID(), attributeQuery.getIssuer().getValue());
    }

    private static void addContextToMdc(String messageType, String messageId, String entityID) {
        MDC.put("messageId", messageId);
        MDC.put("entityId", entityID);
        MDC.put("logPrefix", "[" + messageType + " " + messageId + " from " + entityID + "] ");
    }

}
