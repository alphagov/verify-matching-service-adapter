package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.extensions.PersonName;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder;
import uk.gov.ida.saml.core.transformers.IdentityProviderAssertionUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.HubAssertionUnmarshaller;
import uk.gov.ida.saml.hub.factories.AttributeFactory_1_1;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.jodatime.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.Cycle3DatasetBuilder.aCycle3Dataset;
import static uk.gov.ida.saml.core.test.builders.HubAssertionBuilder.aHubAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

@RunWith(OpenSAMLMockitoRunner.class)
public class InboundMatchingServiceRequestUnmarshallerTest {
    private static final String IDP_ENTITY_ID = "idp-"+UUID.randomUUID().toString();
    private final String FIRST_NAME = "Fred";

    private InboundMatchingServiceRequestUnmarshaller unmarshaller;
    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;
    private String matchingDatasetAssertionId = UUID.randomUUID().toString();
    private String authnStatementAssertionId = UUID.randomUUID().toString();
    private String cycle3DataAssertionId = UUID.randomUUID().toString();

    @Mock
    private HubAssertionUnmarshaller hubAssertionUnmarshaller;
    @Mock
    private IdentityProviderAssertionUnmarshaller identityProviderAssertionUnmarshaller;

    @Before
    public void setup() {
        unmarshaller = new InboundMatchingServiceRequestUnmarshaller(
                hubAssertionUnmarshaller,
                identityProviderAssertionUnmarshaller);

        final IdentityProviderAssertion matchingDatasetAssertion = anIdentityProviderAssertion()
                .withId(matchingDatasetAssertionId)
                .withMatchingDataset(aMatchingDataset().build())
                .build();

        final IdentityProviderAssertion authnStatementAssertion = anIdentityProviderAssertion()
                .withId(authnStatementAssertionId)
                .withAuthnStatement(IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement().build())
                .build();

        final HubAssertion cycle3DataMatchAssertion = aHubAssertion()
                .withId(cycle3DataAssertionId)
                .withCycle3Data(aCycle3Dataset().addCycle3Data("name", "value").build())
                .build();

        when(identityProviderAssertionUnmarshaller.fromAssertion(any(Assertion.class))).thenReturn(matchingDatasetAssertion, authnStatementAssertion);
        when(hubAssertionUnmarshaller.toHubAssertion(any(Assertion.class))).thenReturn(cycle3DataMatchAssertion);
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    }

    @Test
    public void transform_shouldTransformAttributeQueryId() throws Exception {
        AttributeQuery query = givenAValidAttributeQuery();

        InboundMatchingServiceRequest transformedQuery = unmarshaller.fromSaml(
                new ValidatedAttributeQuery(query),
                givenASetOfValidatedHubAssertions(),
                givenASetOfValidatedIdpAssertions());

        assertThat(transformedQuery.getId()).isEqualTo(query.getID());
    }

    @Test
    public void transform_shouldTransformAttributeQueryIssueInstant() throws Exception {
        DateTimeFreezer.freezeTime();

        AttributeQuery query = givenAValidAttributeQuery();

        InboundMatchingServiceRequest transformedQuery = unmarshaller.fromSaml(
                new ValidatedAttributeQuery(query),
                givenASetOfValidatedHubAssertions(),
                givenASetOfValidatedIdpAssertions());

        assertThat(transformedQuery.getIssueInstant()).isEqualTo(DateTime.now());

        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void transform_shouldMapIssuerId() throws Exception {
        AttributeQuery query = givenAValidAttributeQuery();

        InboundMatchingServiceRequest transformedQuery = unmarshaller.fromSaml(
                new ValidatedAttributeQuery(query),
                givenASetOfValidatedHubAssertions(),
                givenASetOfValidatedIdpAssertions());

        assertThat(transformedQuery.getIssuer()).isEqualTo(query.getIssuer().getValue());
    }

    @Test
    public void transform_shouldMapAssertions() throws Exception {
        AttributeQuery query = givenAValidAttributeQuery();

        InboundMatchingServiceRequest transformedQuery = unmarshaller.fromSaml(
                new ValidatedAttributeQuery(query),
                givenASetOfValidatedHubAssertions(),
                givenASetOfValidatedIdpAssertions());

        assertThat(transformedQuery.getMatchingDatasetAssertion()).isNotNull();
        assertThat(transformedQuery.getMatchingDatasetAssertion().getId()).isEqualTo(matchingDatasetAssertionId);
        assertThat(transformedQuery.getAuthnStatementAssertion()).isNotNull();
        assertThat(transformedQuery.getAuthnStatementAssertion().getId()).isEqualTo(authnStatementAssertionId);
        assertThat(transformedQuery.getCycle3AttributeAssertion().isPresent()).isEqualTo(true);
        assertThat(transformedQuery.getCycle3AttributeAssertion().get().getId()).isEqualTo(cycle3DataAssertionId);
    }

    @Test
    public void transform_shouldMapAttributes() {
        AttributeQuery query = givenAValidAttributeQuery();
        InboundMatchingServiceRequest transformedQuery = unmarshaller.fromSaml(
                new ValidatedAttributeQuery(query),
                givenASetOfValidatedHubAssertions(),
                givenASetOfValidatedIdpAssertions());

        assertThat(transformedQuery.getUserCreationAttributes()).hasSize(1);
        PersonName personName = (PersonName) transformedQuery.getUserCreationAttributes().get(0).getAttributeValues().get(0);
        assertThat(personName.getValue()).isEqualTo(FIRST_NAME);

    }

    private ValidatedAssertions givenASetOfValidatedHubAssertions() {
        Assertion cycle3DataAssertion = openSamlXmlObjectFactory.createAssertion();
        Issuer hubIssuer = openSamlXmlObjectFactory.createIssuer(TestEntityIds.HUB_ENTITY_ID);
        cycle3DataAssertion.setIssuer(hubIssuer);
        return new ValidatedAssertions(ImmutableList.of(cycle3DataAssertion));
    }


    private ValidatedAssertions givenASetOfValidatedIdpAssertions() {
        Assertion matchingDatasetAssertion = openSamlXmlObjectFactory.createAssertion();
        Assertion authnStatementAssertion = openSamlXmlObjectFactory.createAssertion();
        Issuer mdsIssuer = openSamlXmlObjectFactory.createIssuer(IDP_ENTITY_ID);
        Issuer authnStatementIssuer = openSamlXmlObjectFactory.createIssuer(IDP_ENTITY_ID);
        matchingDatasetAssertion.setIssuer(mdsIssuer);
        authnStatementAssertion.setIssuer(authnStatementIssuer);
        return new ValidatedAssertions(ImmutableList.of(matchingDatasetAssertion, authnStatementAssertion));
    }

    private AttributeQuery givenAValidAttributeQuery() {
        AttributeQuery query = openSamlXmlObjectFactory.createAttributeQuery();

        query.setIssueInstant(DateTime.now());
        Subject originalSubject = openSamlXmlObjectFactory.createSubject();
        NameID originalSubjectNameId = openSamlXmlObjectFactory.createNameId("name_id");
        Issuer originalIssuer = openSamlXmlObjectFactory.createIssuer("issuer_id");
        originalSubject.setNameID(originalSubjectNameId);

        SubjectConfirmation subjectConfirmation = openSamlXmlObjectFactory.createSubjectConfirmation();
        originalSubject.getSubjectConfirmations().add(subjectConfirmation);
        query.setSubject(originalSubject);
        query.setIssuer(originalIssuer);

        originalIssuer.setValue("original issuer");
        query.setID("original id");
        originalSubjectNameId.setValue("original subject id");
        originalSubjectNameId.setSPNameQualifier("http://foo.com");

        List<Attribute> attributes = query.getAttributes();
        AttributeFactory_1_1 attributeFactory = new AttributeFactory_1_1(openSamlXmlObjectFactory);
        attributes.add(attributeFactory.createFirstnameAttribute(ImmutableList.of(new SimpleMdsValue<>(FIRST_NAME, null, null, false))));

        return query;
    }
}
