package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.domain.AuthnContext.LEVEL_2;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class VerifyAssertionServiceTest {

    private VerifyAssertionService verifyAssertionService;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private ConditionsValidator conditionsValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private VerifyMatchingDatasetUnmarshaller verifyMatchingDatasetUnmarshaller;

    @Mock
    private Cycle3DatasetFactory cycle3DatasetFactory;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        verifyAssertionService = new VerifyAssertionService(
                instantValidator,
                subjectValidator,
                conditionsValidator,
                hubSignatureValidator,
                cycle3DatasetFactory,
                HUB_ENTITY_ID,
                verifyMatchingDatasetUnmarshaller
        );
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        DateTimeFreezer.freezeTime();
    }

    @After
    public void tearDown() {
        DateTimeFreezer.unfreezeTime();
    }
    
    @Test
    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion IssueInstant cannot be null.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id cannot be null or blank.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id cannot be null or blank.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Issuer cannot be null or blank.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Issuer cannot be null or blank.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingHubAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Issuer cannot be null or blank.");
        verifyAssertionService.validateHubAssertion(assertion, "not-used", "", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldCallValidatorsCorrectly() {
        List<Assertion> assertions = asList(
                aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted(),
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted());

        verifyAssertionService.validate("requestId", assertions);
        verify(subjectValidator, times(2)).validate(any(), any());
        verify(hubSignatureValidator, times(2)).validate(any(), any());
    }

    @Test
    public void shouldTranslateVerifyAssertion() {
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        Assertion cycle3Assertion = aCycle3DatasetAssertion("NI", "123456").buildUnencrypted();
        List<Assertion> assertions = asList(
                mdsAssertion,
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted(),
                cycle3Assertion);
        AssertionData assertionData = verifyAssertionService.translate(assertions);

        verify(verifyMatchingDatasetUnmarshaller, times(1)).fromAssertion(mdsAssertion);
        verify(cycle3DatasetFactory, times(1)).createCycle3DataSet(cycle3Assertion);
        assertThat(assertionData.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(assertionData.getMatchingDatasetIssuer()).isEqualTo(STUB_IDP_ONE);

    }

    public static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
            .withId("mds-assertion")
            .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
            .withSubject(anAssertionSubject(requestId))
            .withSignature(signature)
            .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
            .withConditions(aConditions().build());
    }

    public static AssertionBuilder anAuthnStatementAssertion(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                    anAuthnStatement()
                        .withAuthnContext(
                            anAuthnContext()
                                .withAuthnContextClassRef(
                                    anAuthnContextClassRef()
                                        .withAuthnContextClasRefValue(authnContext)
                                        .build())
                                .build())
                        .build())
                .withSubject(
                    aSubject()
                        .withSubjectConfirmation(
                            aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                    aSubjectConfirmationData()
                                        .withInResponseTo(inResponseTo)
                                        .build()
                                    ).build()
                            ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build());
    }

    public static Subject anAssertionSubject(final String inResponseTo) {
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(DateTime.now())
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    public static Signature anIdpSignature() {
        return aSignature().withSigningCredential(
                new TestCredentialFactory(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                    .getSigningCredential()).build();

    }
}