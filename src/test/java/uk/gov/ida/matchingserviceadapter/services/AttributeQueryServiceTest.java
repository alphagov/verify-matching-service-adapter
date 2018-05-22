package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.aMatchingDatasetAssertionWithSignature;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anAuthnStatementAssertion;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anIdpSignature;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class AttributeQueryServiceTest {

    private AttributeQueryService attributeQueryService;

    @Mock
    private AttributeQuerySignatureValidator attributeQuerySignatureValidator;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private VerifyAssertionService verifyAssertionService;

    @Mock
    private EidasAssertionService eidasAssertionService;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    private Assertion eidasAssertion;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        attributeQueryService = new AttributeQueryService(
                attributeQuerySignatureValidator,
                instantValidator,
                verifyAssertionService,
                eidasAssertionService,
                userIdHashFactory
        );
        doNothing().when(attributeQuerySignatureValidator).validate(any());
        doNothing().when(instantValidator).validate(any(), any());
        eidasAssertion = anEidasAssertion("requestId", STUB_COUNTRY_ONE, anEidasSignature());
        when(eidasAssertionService.isCountryAssertion(eidasAssertion)).thenReturn(true);
    }
    @Test
    public void shouldValidateSignatureAndIssueInstant() {
        AttributeQuery attributeQuery = anAttributeQuery().build();

        attributeQueryService.validate(attributeQuery);
        verify(attributeQuerySignatureValidator, times(1)).validate(attributeQuery);
        verify(instantValidator, times(1)).validate(eq(attributeQuery.getIssueInstant()), any());
    }

    @Test
    public void shouldUseVerifyAssertionServiceWhenValidatingVerifyAssertions() {
        List<Assertion> assertions = asList(aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted(),
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted());

        attributeQueryService.validateAssertions("requestId", assertions);
        verify(verifyAssertionService, times(1)).validate("requestId", assertions);
    }


    @Test
    public void shouldUseEidasAssertionServiceWhenValidatingEidasAssertions() {
        List<Assertion> assertions = singletonList(eidasAssertion);

        attributeQueryService.validateAssertions("requestId", assertions);
        verify(eidasAssertionService, times(1)).validate("requestId", assertions);
    }

    public static Signature anEidasSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Assertion anEidasAssertion(String requestId, String issuerId, Signature assertionSignature) {
        return anAssertion()
                .withSubject(
                        aSubject().withSubjectConfirmation(
                                aSubjectConfirmation().withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withInResponseTo(requestId)
                                                .build())
                                        .build())
                                .build())
                .withIssuer(
                        anIssuer()
                                .withIssuerId(issuerId)
                                .build())
                .addAttributeStatement(anEidasAttributeStatement().build())
                .addAuthnStatement(anEidasAuthnStatement().build())
                .withSignature(assertionSignature)
                .withConditions(aConditions().build())
                .buildUnencrypted();
    }
}