package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.validators.assertion.AssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetAttributeStatementBuilder_1_1.aMatchingDatasetAttributeStatement_1_1;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(OpenSAMLMockitoRunner.class)
public class SamlAttributeQueryAssertionsValidatorTest {

    public final String issuerId = "some-entity-id";

    @Mock
    public MatchingServiceAdapterConfiguration configuration;
    @Mock
    public IdentityProviderAssertionValidator identityProviderAssertionValidator;
    @Mock
    public AssertionValidator assertionValidator;

    public SamlAttributeQueryAssertionsValidator validator;

    @Before
    public void setup() {
        validator = new SamlAttributeQueryAssertionsValidator(
                assertionValidator,
                identityProviderAssertionValidator,
                configuration,
                TestEntityIds.HUB_ENTITY_ID
        );
    }

    @Test
    public void decorate_shouldValidateIdentityProviderAssertions() {
        Assertion assertion = anAssertion().withId(UUID.randomUUID().toString()).withIssuer(anIssuer().withIssuerId("foo").build()).buildUnencrypted();
        AttributeQuery query = anAttributeQuery().build();

        validator.validateIdpAssertions(new ValidatedAttributeQuery(query), singletonList(assertion));

        verify(identityProviderAssertionValidator).validate(any(Assertion.class), anyString(), anyString());
    }

    @Test
    public void decorate_shouldValidateHubAssertions() throws Exception {
        Assertion assertion = anAssertion().withId(UUID.randomUUID().toString()).withIssuer(anIssuer().withIssuerId(
                TestEntityIds.HUB_ENTITY_ID).build()).buildUnencrypted();
        AttributeQuery query = anAttributeQuery().build();

        when(configuration.getEntityId()).thenReturn("an-entity-id");
        validator.validateHubAssertions(new ValidatedAttributeQuery(query), singletonList(assertion));

        verify(assertionValidator).validate(any(Assertion.class), anyString(), anyString());
    }

    @Test
    public void decorate_shouldCallTheAssertionValidatorWithTheRequestIdAndEntityFromTheAttributeQuery() {
        final String attributeQueryIssuer = TestEntityIds.HUB_ENTITY_ID;
        final String requestId = "blah";
        Assertion assertion = anAssertion().withId(UUID.randomUUID().toString()).buildUnencrypted();
        AttributeQuery query = anAttributeQuery()
                .withIssuer(anIssuer().withIssuerId(attributeQueryIssuer).build())
                .withId(requestId)
                .build();

        validator.validateIdpAssertions(new ValidatedAttributeQuery(query), singletonList(assertion));

        verify(identityProviderAssertionValidator).validate(any(Assertion.class), eq(requestId), eq(attributeQueryIssuer));
    }

    @Test
    public void decorate_shouldCallTheAssertionValidatorExpectingItselfAsTheRecipientWhenTheAssertionIsNotTheMatchingDatasetAssertion() {
        Assertion cycle3Assertion = aCycle3DatasetAssertion("foo", "bar").buildUnencrypted();
        cycle3Assertion.setIssuer(anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build());
        cycle3Assertion.setSubject(aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withRecipient(issuerId)
                                                .build())
                                .build())
                .build());
        AttributeQuery query = anAttributeQuery()
                .withIssuer(anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build())
                .withId("blah")
                .withSubject(aSubject()
                        .withSubjectConfirmation(aSubjectConfirmation()
                                .withSubjectConfirmationData(aSubjectConfirmationData()
                                        .withRecipient(issuerId)
                                        .build())
                                .build())
                        .build())
                .build();

        when(configuration.getEntityId()).thenReturn("an-entity-id");
        validator.validateHubAssertions(new ValidatedAttributeQuery(query), singletonList(cycle3Assertion));

        verify(assertionValidator).validate(any(Assertion.class), anyString(), anyString());
    }

    @Test
    public void decorate_shouldCallTheAssertionValidatorExpectingTheHubAsTheRecipientWhenTheAssertionIsTheMatchingDatasetAssertion() {
        Assertion assertion = anAssertion()
                .addAttributeStatement(aMatchingDatasetAttributeStatement_1_1().build())
                .withIssuer(anIssuer().withIssuerId(UUID.randomUUID().toString()).build())
                .withSubject(aSubject()
                        .withSubjectConfirmation(
                                aSubjectConfirmation()
                                        .withSubjectConfirmationData(
                                                aSubjectConfirmationData()
                                                        .withRecipient(issuerId)
                                                        .build())
                                        .build())
                        .build())
                .withId(UUID.randomUUID().toString())
                .buildUnencrypted();
        AttributeQuery query = anAttributeQuery()
                .withIssuer(anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build())
                .withId("blah")
                .withSubject(aSubject()
                        .withSubjectConfirmation(aSubjectConfirmation()
                                .withSubjectConfirmationData(aSubjectConfirmationData()
                                        .withRecipient(issuerId)
                                        .build())
                                .build())
                        .build())
                .build();

        validator.validateIdpAssertions(new ValidatedAttributeQuery(query), singletonList(assertion));

        verify(identityProviderAssertionValidator).validate(any(Assertion.class), anyString(), anyString());
    }

}
