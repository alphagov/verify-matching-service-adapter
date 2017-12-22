package uk.gov.ida.matchingserviceadapter.domain;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class MatchingServiceAssertionFactoryTest {
    @Test
    public void createAssertionFromMatchingService() {
        IdGenerator idGenerator = mock(IdGenerator.class);
        AssertionRestrictions assertionRestrictions = mock(AssertionRestrictions.class);
        MatchingServiceAssertionFactory factory = new MatchingServiceAssertionFactory(idGenerator);
        AuthnContext authnContext = AuthnContext.LEVEL_4;
        List<Attribute> userAccountCreationAttributes = Arrays.asList(mock(Attribute.class));

        PersistentId thePersistentId = new PersistentId("thePersistentId");
        MatchingServiceAssertion assertionFromMatchingService = factory.createAssertionFromMatchingService(
            thePersistentId,
            "theIssuerId",
            assertionRestrictions,
            authnContext,
            "theAudience",
            userAccountCreationAttributes
        );

        assertThat(assertionFromMatchingService.getPersistentId(), sameInstance(thePersistentId));
        assertThat(assertionFromMatchingService.getIssuerId(), equalTo("theIssuerId"));
        assertThat(assertionFromMatchingService.getAssertionRestrictions(), sameInstance(assertionRestrictions));
        assertThat(assertionFromMatchingService.getAuthnStatement().getAuthnContext(), sameInstance(authnContext));
        assertThat(assertionFromMatchingService.getAudience(), equalTo("theAudience"));
        assertThat(assertionFromMatchingService.getUserAttributesForAccountCreation(), sameInstance(userAccountCreationAttributes));
    }
}