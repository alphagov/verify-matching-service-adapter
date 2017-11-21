package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.matchingserviceadapter.factories.AttributeQueryAttributeFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;
import static uk.gov.ida.saml.core.test.builders.PersistentIdBuilder.aPersistentId;

public class InboundMatchingServiceRequestBuilder {

    private String id = "id";
    private String issuer = "issuer-id";
    private IdentityProviderAssertion matchingDatasetAssertion = anIdentityProviderAssertion().withMatchingDataset(
            aMatchingDataset().build()).withPersistentId(aPersistentId().build()).build();
    private IdentityProviderAssertion authnStatementAssertion = anIdentityProviderAssertion().withAuthnStatement(anIdentityProviderAuthnStatement().build()).build();
    private Optional<HubAssertion> cycle3AttributeAssertion = absent();
    private String requestIssuerEntityId = "issuer-id";
    private String assertionConsumerServiceUrl = "/foo";
    private List<UserAccountCreationAttribute> userCreationAttributes = ImmutableList.of();

    public static InboundMatchingServiceRequestBuilder anInboundMatchingServiceRequest() {
        return new InboundMatchingServiceRequestBuilder();
    }

    public InboundMatchingServiceRequest build() {
        Iterable<Attribute> requiredAttributes = userCreationAttributes.stream()
                .map(userAccountCreationAttribute -> new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(Collectors.toList());
        return new InboundMatchingServiceRequest(
                id,
                issuer,
                matchingDatasetAssertion,
                authnStatementAssertion,
                cycle3AttributeAssertion,
                DateTime.now(),
                requestIssuerEntityId,
                assertionConsumerServiceUrl,
                ImmutableList.copyOf(requiredAttributes));
    }

    public InboundMatchingServiceRequestBuilder withMatchingDatasetAssertion(IdentityProviderAssertion assertion) {
        this.matchingDatasetAssertion = assertion;
        return this;
    }

    public InboundMatchingServiceRequestBuilder withAuthnStatementAssertion(IdentityProviderAssertion assertion) {
        this.authnStatementAssertion = assertion;
        return this;
    }

    public InboundMatchingServiceRequestBuilder withCycle3DataAssertion(HubAssertion cycle3DataAssertion) {
        this.cycle3AttributeAssertion = fromNullable(cycle3DataAssertion);
        return this;
    }

    public InboundMatchingServiceRequestBuilder withRequestIssuerEntityId(String requestIssuerEntityId) {
        this.requestIssuerEntityId = requestIssuerEntityId;
        return this;
    }

    public InboundMatchingServiceRequestBuilder withAssertionConsumerServiceUrl(String assertionConsumerServiceUrl) {
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        return this;
    }

    public InboundMatchingServiceRequestBuilder withUserCreationAttributes(List<UserAccountCreationAttribute> userCreationAttributes) {
        this.userCreationAttributes = userCreationAttributes;
        return this;
    }
}
