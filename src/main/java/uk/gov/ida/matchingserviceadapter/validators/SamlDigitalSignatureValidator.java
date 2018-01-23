package uk.gov.ida.matchingserviceadapter.validators;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidationParametersCriterion;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.PredicatedValidator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class SamlDigitalSignatureValidator<T extends SignableSAMLObject> extends CompositeValidator<T> {

    public static final MessageImpl DEFAULT_SAML_SIGNATURE_PROFILE_MESSAGE = globalMessage("saml.signature.profile", "Open SAML signature profile validation failed.");

    public SamlDigitalSignatureValidator(
        Message message,
        Function<T, Iterable<Credential>> credentialsProvider,
        Function<T, Issuer> valueProvider,
        QName role) {
        super(
            true,
            new PredicatedValidator<>(DEFAULT_SAML_SIGNATURE_PROFILE_MESSAGE,
                (Predicate<T>) signedObject -> {
                    Signature signature = signedObject.getSignature();
                    SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
                    try {
                        samlSignatureProfileValidator.validate(signature);
                        return true;
                    } catch (SignatureException | ConstraintViolationException e) {
                        return false;
                    }
                }
            ),
            new PredicatedValidator<>(message,
                (Predicate<T>) signableSAMLObject -> {
                    KeyInfoCredentialResolver keyInfoCredResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
                    List<Credential> credentials = new ArrayList<>();
                    credentialsProvider.apply(signableSAMLObject).forEach(credentials::add);
                    StaticCredentialResolver staticCredentialResolver = new StaticCredentialResolver(credentials);
                    ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(staticCredentialResolver, keyInfoCredResolver);
                    SignatureValidationParameters signatureValidationParameters = new SignatureValidationParameters();
                    signatureValidationParameters.setWhitelistedAlgorithms(Arrays.asList(
                        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
                        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
                        SignatureConstants.ALGO_ID_DIGEST_SHA1,
                        SignatureConstants.ALGO_ID_DIGEST_SHA256,
                        SignatureConstants.ALGO_ID_DIGEST_SHA512
                    ));
                    CriteriaSet criteriaSet = new CriteriaSet();
                    criteriaSet.add(new EntityIdCriterion(valueProvider.apply(signableSAMLObject).getValue()));
                    criteriaSet.add(new EntityRoleCriterion(role));
                    criteriaSet.add(new SignatureValidationParametersCriterion(signatureValidationParameters));
                    try {
                        return trustEngine.validate(signableSAMLObject.getSignature(), criteriaSet);
                    } catch (SecurityException e) {
                        return false;
                    }
                }
            )
        );
    }
}
