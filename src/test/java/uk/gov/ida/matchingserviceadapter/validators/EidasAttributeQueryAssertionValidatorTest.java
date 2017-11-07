package uk.gov.ida.matchingserviceadapter.validators;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.beanplanet.messages.domain.Messages;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateEmptyIssuerMessage;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;

@RunWith(MockitoJUnitRunner.class)
public class EidasAttributeQueryAssertionValidatorTest {

    public static final String TYPE_OF_ASSERTION = "Identity";
    @Mock
    private MetadataResolver metadataResolver;

    @Mock
    private CertificateValidator certificateValidator;

    @Mock
    private CertificateExtractor certificateExtractor;

    @Mock
    private EntityDescriptor entityDescriptor;

    private X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private EidasAttributeQueryAssertionValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new EidasAttributeQueryAssertionValidator(
            metadataResolver,
            certificateValidator,
            certificateExtractor,
            x509CertificateFactory,
            new DateTimeComparator(Duration.ZERO),
            TYPE_OF_ASSERTION);
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldValidateIssuer() {
        Assertion assertion = anAssertion().withIssuer(anIssuer().withIssuerId("").build()).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(generateEmptyIssuerMessage(TYPE_OF_ASSERTION))).isTrue();
    }

    @Test
    public void shouldValidateSubject() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));
        Assertion assertion = anAssertion().withSubject(null).buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(1);
        assertThat(messages.hasErrorLike(SUBJECT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldValidateSignature() throws Exception {
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(certificateExtractor.extractIdpSigningCertificates(entityDescriptor))
            .thenReturn(Arrays.asList(new Certificate(TEST_ENTITY_ID, TestCertificateStrings.TEST_PUBLIC_CERT, Certificate.KeyUse.Signing)));

        Assertion assertion = anAssertion().buildUnencrypted();

        Messages messages = validator.validate(assertion, messages());

        assertThat(messages.size()).isEqualTo(0);
        assertThat(messages.hasErrors()).isFalse();
    }
}
