package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;

import java.util.List;
import java.util.Optional;

public class InboundVerifyMatchingServiceRequest extends InboundMatchingServiceRequest {
    private final IdentityProviderAssertion matchingDatasetAssertion;
    private final IdentityProviderAssertion authnStatementAssertion;

    public InboundVerifyMatchingServiceRequest(
            String id,
            String issuer,
            IdentityProviderAssertion matchingDatasetAssertion,
            IdentityProviderAssertion authnStatementAssertion,
            Optional<HubAssertion> cycle3AttributeAssertion,
            DateTime issueInstant,
            String authnRequestIssuerId,
            String assertionConsumerServiceUrl,
            List<Attribute> userCreationAttributes) {


        super(id, issuer, cycle3AttributeAssertion, issueInstant, authnRequestIssuerId, assertionConsumerServiceUrl, userCreationAttributes);

        this.matchingDatasetAssertion = matchingDatasetAssertion;
        this.authnStatementAssertion = authnStatementAssertion;
    }

    public IdentityProviderAssertion getMatchingDatasetAssertion() {
        return matchingDatasetAssertion;
    }

    public IdentityProviderAssertion getAuthnStatementAssertion() {
        return authnStatementAssertion;
    }
}