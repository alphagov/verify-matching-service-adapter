package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.x509.BasicX509Credential;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;

import java.time.Duration;
import java.util.stream.Collectors;

import static uk.gov.ida.matchingserviceadapter.validators.IssueInstantValidator.IssueInstantJodaDateTimeValidator;
import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class EidasAttributeQueryAssertionValidator extends CompositeValidator<Assertion> {

    public EidasAttributeQueryAssertionValidator(final MetadataResolver metadataResolver,
                                                 final CertificateValidator certificateValidator,
                                                 final CertificateExtractor certificateExtractor,
                                                 final X509CertificateFactory x509CertificateFactory,
                                                 final DateTimeComparator dateTimeComparator,
                                                 final String typeOfAssertion,
                                                 final String hubConnectorEntityId,
                                                 final Duration ttl,
                                                 final Duration clockDelta) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(
                    generateMissingIssuerMessage(typeOfAssertion),
                    generateEmptyIssuerMessage(typeOfAssertion),
                    Assertion::getIssuer
                ),
                new SamlDigitalSignatureValidator<>(
                    generateInvalidSignatureMessage(typeOfAssertion),
                    assertion -> new MetadataCertificatesRepository(metadataResolver, certificateValidator, certificateExtractor)
                        .getIdpSigningCertificates(assertion.getIssuer().getValue()).stream()
                        .map(Certificate::getCertificate)
                        .map(x509CertificateFactory::createCertificate)
                        .map(BasicX509Credential::new)
                        .collect(Collectors.toList()),
                    Assertion::getIssuer,
                    IDPSSODescriptor.DEFAULT_ELEMENT_NAME
                )
            ),
            new SubjectValidator<>(Assertion::getSubject, dateTimeComparator),
            IssueInstantJodaDateTimeValidator(
                globalMessage("expired.message", "Issue Instant time-to-live has been exceeded"),
                globalMessage("issue.instance.in.future", "Issue Instant is in the future"),
                Assertion::getIssueInstant,
                ttl,
                clockDelta
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(a -> a.getAuthnStatements().size() != 1, generateWrongNumberOfAuthnStatementsMessage(typeOfAssertion)),
                new AuthnStatementValidator<>(a -> a.getAuthnStatements().get(0), dateTimeComparator)
            ),
            new ConditionsValidator<>(Assertion::getConditions, hubConnectorEntityId)
        );
    }

    public static MessageImpl generateEmptyIssuerMessage(final String typeOfAssertion) {
        return fieldMessage("issuer.value", "issuer.value.empty", typeOfAssertion + " Assertion's issuer was empty.");
    }

    public static MessageImpl generateMissingIssuerMessage(final String typeOfAssertion) {
        return fieldMessage("issuer", "issuer.empty", typeOfAssertion + " Assertion's issuer was not provided.");
    }

    public static MessageImpl generateInvalidSignatureMessage(final String typeOfAssertion) {
        return globalMessage("invalid.signature", typeOfAssertion + " Assertion's signature was invalid.");
    }

    public static MessageImpl generateWrongNumberOfAuthnStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("issuer.authnStatements", "issuer.authnStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of authn statements.");
    }
}
