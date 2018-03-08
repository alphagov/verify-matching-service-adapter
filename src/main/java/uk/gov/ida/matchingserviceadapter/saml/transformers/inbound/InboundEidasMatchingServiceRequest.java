package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.saml.core.domain.HubAssertion;

import java.util.List;

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