package uk.gov.ida.integrationtest.interfacetests;

import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.security.credential.Credential;

import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.TestCredentialFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithEncryptedAssertions;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentGivenNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentFamilyNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aDateOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;

@Ignore("Unmarshalling of MatchingDatasets is not fully working so ignore these tests temporarily")
public class EidasExampleSchemaTests extends BaseTestToolInterfaceTest {
    private static final String REQUEST_ID = "default-match-id";
    private static final String PID = "default-pid";

    private static final Credential MSA_ENCRYPTION_CREDENTIAL = new TestCredentialFactory(
        TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
        null)
        .getEncryptingCredential();

    private static final Credential COUNTRY_SIGNING_CREDENTIAL = new TestCredentialFactory(
            TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT, // TODO: change this to Stub Country
            TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
            .getSigningCredential();

    @ClassRule
    public static final MatchingServiceAdapterAppRule appRule = new MatchingServiceAdapterAppRule(true, configRules);

    @Override
    protected DropwizardAppRule<MatchingServiceAdapterConfiguration> getAppRule() { return appRule; }

    @Test
    public void shouldProduceLoA2StandardDataset() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(asList(
                anEidasAssertion()
                    .withConditions(
                        aConditions()
                        .validFor(Duration.standardMinutes(10))
                        .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getHubConnectorEntityId())
                        .build())
                    .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                    .withSignature(aSignature().withSigningCredential(COUNTRY_SIGNING_CREDENTIAL).build())
                    .withoutAttributeStatements()
                    .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                        aCurrentGivenNameAttribute("Joe"),
                        aCurrentFamilyNameAttribute("Dou"),
                        aDateOfBirthAttribute(new LocalDate(1980, 5, 24)),
                        aPersonIdentifierAttribute(PID)
                    )).build()
                ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2StandardDatasetWithTransliterationProvidedForNameFields() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(asList(
                anEidasAssertion()
                    .withConditions(
                        aConditions()
                        .validFor(Duration.standardMinutes(10))
                        .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getHubConnectorEntityId())
                        .build())
                    .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                    .withSignature(aSignature().withSigningCredential(COUNTRY_SIGNING_CREDENTIAL).build())
                    .withoutAttributeStatements()
                    .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                        aCurrentGivenNameAttribute("Georgios", "Γεώργιος"),
                        aCurrentFamilyNameAttribute("Panathinaikos", "Παναθηναϊκός"),
                        aDateOfBirthAttribute(new LocalDate(1980, 5, 24)),
                        aPersonIdentifierAttribute(PID)
                    )).build()
                ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set-transliteration_provided_for_name_fields.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2StandardDatasetWithSpecialCharactersInNameFields() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithEncryptedAssertions(asList(
                anEidasAssertion()
                    .withConditions(
                        aConditions()
                        .validFor(Duration.standardMinutes(10))
                        .restrictedToAudience(appRule.getConfiguration().getEuropeanIdentity().getHubConnectorEntityId())
                        .build())
                    .withIssuer(anIssuer().withIssuerId(appRule.getCountryEntityId()).build())
                    .withSignature(aSignature().withSigningCredential(COUNTRY_SIGNING_CREDENTIAL).build())
                    .withoutAttributeStatements()
                    .addAttributeStatement(anAttributeStatement().addAllAttributes(asList(
                        aCurrentGivenNameAttribute("Šarlota"),
                        aCurrentFamilyNameAttribute("Snježana"),
                        aDateOfBirthAttribute(new LocalDate(1980, 5, 24)),
                        aPersonIdentifierAttribute(PID)
                    )).build()
                ).buildWithEncrypterCredential(MSA_ENCRYPTION_CREDENTIAL)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/universal-dataset/eIDAS-LoA2-Standard_data_set-special_characters_in_name_fields.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }
}
