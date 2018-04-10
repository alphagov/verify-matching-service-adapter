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
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
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

    public SamlDigitalSignatureValidator(
        Message message,
        SignatureValidator signaturevalidator,
        Function<T, Issuer> valueProvider,
        QName role) {
        super(
            true,
            new PredicatedValidator<>(message,
                (Predicate<T>) signableSAMLObject -> {
                    try {
                        return signaturevalidator.validate(signableSAMLObject, valueProvider.apply(signableSAMLObject).getValue(), role);
                    } catch (SecurityException | SignatureException e) {
                        throw new RuntimeException(e);
                    }
                }
            )
        );
    }
}
