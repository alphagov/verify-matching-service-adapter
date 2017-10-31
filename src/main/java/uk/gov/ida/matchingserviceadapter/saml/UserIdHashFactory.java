package uk.gov.ida.matchingserviceadapter.saml;

import com.google.common.base.Optional;
import org.apache.commons.codec.binary.Hex;
import org.opensaml.security.crypto.JCAConstants;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import static com.google.common.base.Throwables.propagate;

public class UserIdHashFactory {
    public String createHashedId(String partnerEntityId, String entityId, String persistentId, Optional<IdentityProviderAuthnStatement> authnStatement) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(JCAConstants.DIGEST_SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw propagate(e);
        }

        final String persistentIdHash = getPersistentIdHashForDigest(partnerEntityId, entityId, persistentId, authnStatement);

        try {
            md.update(persistentIdHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw propagate(e);
        }

        byte[] digest = md.digest();
        return Hex.encodeHexString(digest);
    }

    private String getPersistentIdHashForDigest(String partnerEntityId, String entityId, String persistentId, Optional<IdentityProviderAuthnStatement> authnStatement) {
        String persistentIdHash;

        final AuthnContext authnContext = authnStatement.get().getAuthnContext();
        if(authnContext.equals(AuthnContext.LEVEL_2)) {
            // default behaviour - for LEVEL_2
            persistentIdHash = MessageFormat.format("{0}{1}{2}", partnerEntityId, entityId, persistentId);
        } else {
            // if we have an authnContext that is not LEVEL_2 then regenerate the hash
            // this does not break existing behaviour for LEVEL_2 RPs
            persistentIdHash = MessageFormat.format("{0}{1}{2}{3}", partnerEntityId, entityId, persistentId, authnContext.name());
        }
        return persistentIdHash;
    }
}
