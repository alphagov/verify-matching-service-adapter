package uk.gov.ida.matchingserviceadapter.builders;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.ida.matchingserviceadapter.builders.PersistentIdBuilder.aPersistentId;
import static uk.gov.ida.saml.core.test.builders.AssertionRestrictionsBuilder.anAssertionRestrictions;

public class MatchingServiceAssertionBuilder {

    private List<Attribute> userAttributesForAccountCreation = new ArrayList<>();
    private String id = "assertion issuer id";
    private String issuerId = "assertion-id" + UUID.randomUUID();
    private DateTime issueInstant = DateTime.now();

    public static MatchingServiceAssertionBuilder aMatchingServiceAssertion() {
        return new MatchingServiceAssertionBuilder();
    }

    public MatchingServiceAssertion build() {
        return new MatchingServiceAssertion(
                id,
                issuerId,
                issueInstant,
                aPersistentId().build(),
                anAssertionRestrictions().build(),
                MatchingServiceAuthnStatementBuilder.anIdaAuthnStatement().build(),
                "authn-request-issuer-entity-id",
                userAttributesForAccountCreation);
    }

    public MatchingServiceAssertionBuilder withUserAttributesForAccountCreation(List<Attribute> attributesForAccountCreation){
        userAttributesForAccountCreation = attributesForAccountCreation;
        return this;
    }

}
