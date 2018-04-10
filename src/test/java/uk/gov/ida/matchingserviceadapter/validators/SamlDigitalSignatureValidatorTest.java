package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;
import uk.gov.ida.saml.core.test.validators.SingleCertificateSignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class SamlDigitalSignatureValidatorTest {
    private static final MessageImpl EXPECTED_MESSAGE = globalMessage("theCode", "theMessage");
    private SamlDigitalSignatureValidator<AttributeQuery> validator;

    @BeforeClass
    public static void setUpAll() {
        IdaSamlBootstrap.bootstrap();
    }

    @Before
    public void setUp() throws Exception {
        SignatureValidator signatureValidator = new SingleCertificateSignatureValidator(new BasicX509Credential(new X509CertificateFactory().createCertificate(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT)));
        validator = new SamlDigitalSignatureValidator<>(EXPECTED_MESSAGE, signatureValidator, AttributeQuery::getIssuer, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldApplyOpenSamlSignatureProfileValidator() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery().withSignature(null).build();

        assertThatThrownBy(() -> {
            validator.validate(attributeQuery, messages());
        }).hasCauseInstanceOf(SignatureException.class).hasMessage("org.opensaml.xmlsec.signature.support.SignatureException: Signature in signableSAMLObject is null");
    }

    @Test
    public void shouldValidateSignatureSuccessfully() {
        AttributeQuery attributeQuery = createAttributeQuery(HUB_TEST_PUBLIC_SIGNING_CERT, HUB_TEST_PRIVATE_SIGNING_KEY);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldReturnAnErrorWhenSignatureValidationFails() {
        AttributeQuery attributeQuery = createAttributeQuery(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY);

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(EXPECTED_MESSAGE)).isTrue();
    }

    private Function<AttributeQuery, Iterable<Credential>> createCredentialProvider(final String certificate) {
        return aqr -> asList(
            new X509CertificateFactory().createCertificate(certificate)
        ).stream().map(BasicX509Credential::new).collect(Collectors.toList());
    }

    private AttributeQuery createAttributeQuery(final String publicSigningKey, final String privateSigningKey) {
        return AttributeQueryBuilder.anAttributeQuery()
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            publicSigningKey,
                            privateSigningKey
                        ).getSigningCredential()
                    ).build()
            )
            .build();
    }
}
