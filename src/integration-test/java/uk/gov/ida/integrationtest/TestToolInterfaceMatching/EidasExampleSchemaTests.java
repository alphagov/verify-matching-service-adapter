package uk.gov.ida.integrationtest.TestToolInterfaceMatching;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeBuilder_1_1.anAddressAttribute;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1.aDate_1_1;
import static uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder.aDateValue;
import static uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1.aGender_1_1;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;

public class EidasExampleSchemaTests extends BaseTestToolInterfaceTest {
    private static final String REQUEST_ID = "default-match-id";
    private static final String PID = "default-pid";

    @Test
    public void shouldProduceLoA2SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .build())
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(true).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .build()).buildAsDateOfBirth()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/LoA2-simple-case.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2SimpleExcludingOptionalAddressFieldsCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .build())
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(true).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .withPostcode(null)
                            .withInternationalPostcode(null)
                            .withUprn(null)
                            .withVerified(true)
                            .build())
                        .buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/simple-case-excluding-optional-address-fields.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }
    @Test
    public void shouldProduceLoA1SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .build())
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(true).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/LoA1-simple-case.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA1ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(true)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Dou")
                                .withVerified(true)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("John")
                                .withVerified(true)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Joe")
                                .withVerified(true)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Simon")
                                .withVerified(true)
                                .build()
                        )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(true).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("10 George Street"))
                                .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(2007, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("344 George Street"))
                                .withFrom(new DateTime(2009, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(getDateReplacement(inRange405to100))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 2PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("67676 George Street"))
                                .withFrom(getDateReplacement(inRange405to101))
                                .withTo(getDateReplacement(inRange405to200))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("46244 George Street"))
                                .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                                .withFrom(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1989, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withPostcode(null)
                                .withInternationalPostcode(null)
                                .withUprn(null)
                                .withVerified(false)
                                .build()
                        ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/LoA1-extensive-case.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }
/*
    @Test
    public void shouldProduceLoA2ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Bob Rob")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsMiddlename(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(false)
                                .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1987, 1, 20, 0, 0, DateTimeZone.UTC))
                                .build()
                        ).addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(false)
                            .withFrom(getDateReplacement(yesterday))
                            .withTo(null)
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("John")
                            .withVerified(true)
                            .withFrom(new DateTime(2003, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(2004, 1, 20, 0, 0, DateTimeZone.UTC))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(new DateTime(2005, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(getDateReplacement(inRange180to100))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Simon")
                            .withVerified(false)
                            .withFrom(getDateReplacement(inRange180to101))
                            .withTo(getDateReplacement(inRange180to150))
                            .build()
                    )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(false).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("10 George Street"))
                                .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(2007, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("344 George Street"))
                            .withFrom(new DateTime(2009, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(getDateReplacement(inRange405to100))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(true)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("67676 George Street"))
                            .withFrom(getDateReplacement(inRange405to101))
                            .withTo(getDateReplacement(inRange405to200))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("56563 George Street"))
                            .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                            .withFrom(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(1989, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withPostcode(null)
                            .withInternationalPostcode(null)
                            .withUprn(null)
                            .withVerified(false)
                            .build()
                    ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/LoA2-extensive-case.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceUserAccountCreationJson() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(asList())
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(), false, REQUEST_ID)
            ), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path filePath = Paths.get("verify-matching-service-test-tool/src/main/resources/eidas/user-account-creation.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, filePath, UNKNOWN_USER_URI);
    }
*/
}

