package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.aMatchingDatasetAssertionWithSignature;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anAuthnStatementAssertion;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anIdpSignature;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;

public class AttributeQueryServiceTest {

    private AttributeQueryService attributeQueryService;

    @Mock
    private AttributeQuerySignatureValidator attributeQuerySignatureValidator;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private VerifyAssertionService verifyAssertionService;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        attributeQueryService = new AttributeQueryService(
                attributeQuerySignatureValidator,
                instantValidator,
                verifyAssertionService,
                userIdHashFactory,
                HUB_ENTITY_ID
        );
        doNothing().when(attributeQuerySignatureValidator).validate(any());
        doNothing().when(instantValidator).validate(any(), any());
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

}