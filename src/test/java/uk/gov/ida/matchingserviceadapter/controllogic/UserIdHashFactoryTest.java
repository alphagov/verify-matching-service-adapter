package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.base.Optional;
import org.junit.Test;
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
    @Test
    public void createHashedId_shouldCallMessageDigest() throws Exception {
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";
        final String entityId = "entity";

        final String hashedId = new UserIdHashFactory().createHashedId(partnerEntityId, entityId, persistentId.getNameId(), Optional.of(IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement(AuthnContext.LEVEL_2, new IpAddress("ipaddress"))));

        assertThat(hashedId).isEqualTo("a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298");
    }

    @Test
    public void createHashedId_shouldGenerateADifferentHashForEveryAuthnContext() throws Exception {
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";
        final String entityId = "entity";

        final UserIdHashFactory userIdHashFactory = new UserIdHashFactory();

        final long numberOfUniqueGeneratedHashedPids = Arrays.stream(AuthnContext.values())
                .map(authnContext -> userIdHashFactory.createHashedId(partnerEntityId, entityId, persistentId.getNameId(), Optional.of(IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement(authnContext, new IpAddress("ipaddress")))))
                .distinct()
                .count();

        assertThat(numberOfUniqueGeneratedHashedPids).isEqualTo(5);
    }
}
