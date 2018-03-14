package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.saml.core.domain.HubAssertion;

import java.util.List;
import java.util.Optional;

public class InboundEidasMatchingServiceRequest extends InboundMatchingServiceRequest {

    private ProxyNodeAssertion proxyNodeAssertion;

    public InboundEidasMatchingServiceRequest(
            String id,
            String issuer,
            ProxyNodeAssertion proxyNodeAssertion,
            Optional<HubAssertion> cycle3AttributeAssertion,
            DateTime issueInstant,
            String authnRequestIssuerId,
            String assertionConsumerServiceUrl,
            List<Attribute> userCreationAttributes) {

        super(id, issuer, cycle3AttributeAssertion, issueInstant, authnRequestIssuerId, assertionConsumerServiceUrl, userCreationAttributes);
        this.proxyNodeAssertion = proxyNodeAssertion;
    }

    public ProxyNodeAssertion getMatchingDatasetAssertion() {
        return proxyNodeAssertion;
    }
}