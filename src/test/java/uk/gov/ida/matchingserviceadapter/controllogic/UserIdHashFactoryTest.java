package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.IpAddress;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.PersistentIdBuilder.aPersistentId;

@RunWith(MockitoJUnitRunner.class)
public class UserIdHashFactoryTest {

    private static final String MSA_ENTITY_ID = "entity";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private UserIdHashFactory userIdHashFactory = new UserIdHashFactory(MSA_ENTITY_ID);

    @Test
    public void createHashedId_shouldCallMessageDigest() throws Exception {
        final PersistentId persistentId = aPersistentId().build();
        final String issuerId = "partner";

        final String hashedId = userIdHashFactory.hashId(issuerId, persistentId.getNameId(), Optional.of(IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement(AuthnContext.LEVEL_2, new IpAddress("ipaddress"))).transform(IdentityProviderAuthnStatement::getAuthnContext));

        assertThat(hashedId).isEqualTo("a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298");
    }

    @Test
    public void createHashedId_shouldGenerateADifferentHashForEveryAuthnContext() throws Exception {
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";
        final String entityId = "entity";

        final long numberOfUniqueGeneratedHashedPids = Arrays.stream(AuthnContext.values())
                .map(authnContext -> userIdHashFactory.hashId(partnerEntityId, persistentId.getNameId(), Optional.of(IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement(authnContext, new IpAddress("ipaddress"))).transform(IdentityProviderAuthnStatement::getAuthnContext)))
                .distinct()
                .count();

        assertThat(numberOfUniqueGeneratedHashedPids).isEqualTo(5);
    }

    @Test
    public void shouldThrowIfAuthnContextAbsent() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(String.format("Authn context absent for persistent id %s", "pid"));

        userIdHashFactory.hashId("", "pid", Optional.absent());
    }
}
