package uk.gov.ida.matchingserviceadapter.domain;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.util.List;

public class MatchingServiceAssertionFactory {

    private final IdGenerator idGenerator;

    @Inject
    public MatchingServiceAssertionFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public MatchingServiceAssertion createAssertionFromMatchingService(
            PersistentId persistentId,
            String issuerId,
            AssertionRestrictions assertionRestrictions,
            AuthnContext authnContext,
            String audience,
            List<Attribute> userAttributesForAccountCreation) {

        return new MatchingServiceAssertion(
                idGenerator.getId(),
                issuerId,
                DateTime.now(),
                persistentId,
                assertionRestrictions,
                MatchingServiceAuthnStatement.createIdaAuthnStatement(authnContext),
                audience,
                userAttributesForAccountCreation);
    }
}
