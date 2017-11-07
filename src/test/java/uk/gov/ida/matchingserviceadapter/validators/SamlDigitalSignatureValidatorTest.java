package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;

public class SamlDigitalSignatureValidatorTest {
    private static final MessageImpl EXPECTED_MESSAGE = globalMessage("theCode", "theMessage");
    private SamlDigitalSignatureValidator<AttributeQuery> validator;

    @Before
    public void setUp() throws Exception {
        Function<AttributeQuery, Iterable<Credential>> credentialProvider = createCredentialProvider(HUB_TEST_PUBLIC_SIGNING_CERT);
        validator = new SamlDigitalSignatureValidator<>(EXPECTED_MESSAGE, credentialProvider, AttributeQuery::getIssuer, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldApplyOpenSamlSignatureProfileValidator() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery().withSignature(null).build();

        Messages messages = validator.validate(attributeQuery, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(SamlDigitalSignatureValidator.DEFAULT_SAML_SIGNATURE_PROFILE_MESSAGE)).isTrue();
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
        return aqr -> Arrays.asList(
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
