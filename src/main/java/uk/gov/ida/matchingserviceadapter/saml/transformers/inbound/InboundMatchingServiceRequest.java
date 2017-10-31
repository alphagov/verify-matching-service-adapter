package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdaSamlMessage;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;

import java.util.List;

public class InboundMatchingServiceRequest extends IdaSamlMessage {
    private final IdentityProviderAssertion matchingDatasetAssertion;
    private final IdentityProviderAssertion authnStatementAssertion;
    private final Optional<HubAssertion> cycle3AttributeAssertion;
    private final String authnRequestIssuerId;
    private final String assertionConsumerServiceUrl;
    private final List<Attribute> userCreationAttributes;

    public InboundMatchingServiceRequest(
            String id,
            String issuer,
            IdentityProviderAssertion matchingDatasetAssertion,
            IdentityProviderAssertion authnStatementAssertion,
            Optional<HubAssertion> cycle3AttributeAssertion,
            DateTime issueInstant,
            String authnRequestIssuerId,
            String assertionConsumerServiceUrl,
            List<Attribute> userCreationAttributes) {


        super(id, issuer, issueInstant, null);

        this.matchingDatasetAssertion = matchingDatasetAssertion;
        this.authnStatementAssertion = authnStatementAssertion;
        this.cycle3AttributeAssertion = cycle3AttributeAssertion;
        this.authnRequestIssuerId = authnRequestIssuerId;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.userCreationAttributes = userCreationAttributes;
    }

    public IdentityProviderAssertion getMatchingDatasetAssertion() {
        return matchingDatasetAssertion;
    }

    public IdentityProviderAssertion getAuthnStatementAssertion() {
        return authnStatementAssertion;
    }

    public Optional<HubAssertion> getCycle3AttributeAssertion() {
        return cycle3AttributeAssertion;
    }

    public String getAuthnRequestIssuerId() {
        return authnRequestIssuerId;
    }

    public String getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }

    public List<Attribute> getUserCreationAttributes() {
        return userCreationAttributes;
    }
}