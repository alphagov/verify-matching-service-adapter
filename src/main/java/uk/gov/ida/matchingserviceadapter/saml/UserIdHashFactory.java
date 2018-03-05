package uk.gov.ida.matchingserviceadapter.saml;

import com.google.common.base.Optional;
import org.apache.commons.codec.binary.Hex;
import org.opensaml.security.crypto.JCAConstants;
import uk.gov.ida.matchingserviceadapter.exceptions.AuthnContextMissingException;
import uk.gov.ida.saml.core.domain.AuthnContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.MSA_ENTITY_ID;

public class UserIdHashFactory {

    private final String msaEntityId;

    @Inject
    public UserIdHashFactory(@Named(MSA_ENTITY_ID) String msaEntityId) {
        this.msaEntityId = msaEntityId;
    }

    public String hashId(String issuerEntityId, String persistentId, Optional<AuthnContext> authnContext) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(JCAConstants.DIGEST_SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final String toHash = idToHash(issuerEntityId, persistentId, authnContext);

        try {
            messageDigest.update(toHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        byte[] digest = messageDigest.digest();
        return Hex.encodeHexString(digest);
    }

    private String idToHash(String issuerEntityId, String persistentId, Optional<AuthnContext> context) {
        String persistentIdHash;

        final AuthnContext authnContext = context.toJavaUtil().orElseThrow(() -> new AuthnContextMissingException(String.format("Authn context absent for persistent id %s", persistentId)));
        if(authnContext.equals(AuthnContext.LEVEL_2)) {
            // default behaviour - for LEVEL_2
            persistentIdHash = MessageFormat.format("{0}{1}{2}", issuerEntityId, msaEntityId, persistentId);
        } else {
            // if we have an authnContext that is not LEVEL_2 then regenerate the hash
            // this does not break existing behaviour for LEVEL_2 RPs
            persistentIdHash = MessageFormat.format("{0}{1}{2}{3}", issuerEntityId, msaEntityId, persistentId, authnContext.name());
        }
        return persistentIdHash;
    }
}
