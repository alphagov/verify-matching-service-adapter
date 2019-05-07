package uk.gov.ida.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import httpstub.HttpStubRule;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder;
import uk.gov.ida.integrationtest.helpers.AssertionHelper;
import uk.gov.ida.integrationtest.helpers.AttributeFactory;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.security.AssertionDecrypter;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.RESPONDER;
import static org.opensaml.saml.saml2.core.StatusCode.SUCCESS;
import static uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder.aUserAccountCreationAttributeValue;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aCompleteMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.assertionWithOnlyFirstName;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.integrationtest.helpers.UserAccountCreationTestAssertionHelper.assertThatResponseContainsExpectedUserCreationAttributes;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.*;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.CREATED;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.CREATE_FAILURE;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static uk.gov.ida.saml.core.test.builders.VerifiedAttributeValueBuilder.aVerifiedValue;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;

public abstract class UserAccountCreationBaseTest {
    protected static final String UNKNOWN_USER_MATCHING_PATH = "/unknown-user-attribute-query";
    protected static final String MATCHING_REQUEST_PATH = "/matching-request";
    protected static final String REQUEST_ID = "default-request-id";
    protected String UNKNOWN_USER_URI;

    protected AssertionDecrypter assertionDecrypter;
    protected final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    protected final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    private MatchingServiceAdapterAppRule matchingServiceAdapterAppRule;
    private HttpStubRule localMatchingService;

    @Before
    public void setUp() throws Exception {
        assertionDecrypter = AssertionHelper.anAssertionDecrypter();
        localMatchingService = setUpMatchingService();
        matchingServiceAdapterAppRule = getAppRule();
        UNKNOWN_USER_URI = "http://localhost:" + matchingServiceAdapterAppRule.getLocalPort() + "/unknown-user-attribute-query";
    }

    protected abstract MatchingServiceAdapterAppRule getAppRule();
    protected abstract HttpStubRule setUpMatchingService() throws Exception;

    @Test
    public void shouldReturnCurrentAttributesWhenPassedFullMatchingDataset() {
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(
                FIRST_NAME, FIRST_NAME_VERIFIED, MIDDLE_NAME, MIDDLE_NAME_VERIFIED, SURNAME, SURNAME_VERIFIED, CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY, CYCLE_3));
        AttributeQuery attributeQuery = anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(requiredAttributes)
            .withIssuer(anIssuer().withIssuerId(matchingServiceAdapterAppRule.getConfiguration().getHubEntityId()).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion("default-request-id"),
                aCompleteMatchingDatasetAssertion(REQUEST_ID),
                AssertionBuilder.aCycle3DatasetAssertion("cycle3Name", "cycle3Value").buildUnencrypted()), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATED);
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

        assertThatResponseContainsExpectedUserCreationAttributes(decryptedAssertions.get(0).getAttributeStatements(), ImmutableList.of(
            userAccountCreationAttributeFor(aPersonNameValue().withValue("CurrentSurname").build(), SURNAME),
            userAccountCreationAttributeFor(aVerifiedValue().withValue(true).build(), SURNAME_VERIFIED),
            userAccountCreationAttributeFor(aPersonNameValue().withValue("FirstName").build(), FIRST_NAME),
            userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), FIRST_NAME_VERIFIED),
            userAccountCreationAttributeFor(anAddressAttributeValue().addLines(ImmutableList.of("address line 1")).withVerified(false).build(), CURRENT_ADDRESS),
            userAccountCreationAttributeFor(aVerifiedValue().withValue(false).build(), CURRENT_ADDRESS_VERIFIED),
            userAccountCreationAttributeFor(
                asList(
                    anAddressAttributeValue().addLines(ImmutableList.of("address line 1")).withVerified(false).build(),
                    anAddressAttributeValue().addLines(ImmutableList.of("address line 2")).withVerified(true).build()
                ),
                ADDRESS_HISTORY),
            userAccountCreationAttributeFor(openSamlXmlObjectFactory.createSimpleMdsAttributeValue("cycle3Value"), CYCLE_3)
        ));
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }


    @Test
    public void shouldReturnFailureResponseWhenAttributesRequestedDoNotExist(){
        List<Attribute> requiredAttributes = attributesFromUacAttributes(Stream.of(FIRST_NAME, MIDDLE_NAME));
        AttributeQuery attributeQuery = anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(requiredAttributes)
            .withIssuer(anIssuer().withIssuerId(matchingServiceAdapterAppRule.getConfiguration().getHubEntityId()).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion("default-request-id"),
                assertionWithOnlyFirstName(REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID))
            .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(decryptedAssertions).hasSize(0);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnResponderStatusCodeWhenLocalMatchingServiceIsDown() throws JsonProcessingException {
        localMatchingService.reset();
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"failure\"}");

        List<Attribute> requiredAttributes = singletonList(new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(FIRST_NAME));

        AttributeQuery attributeQuery = anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(requiredAttributes)
            .withIssuer(anIssuer().withIssuerId(matchingServiceAdapterAppRule.getConfiguration().getHubEntityId()).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion("default-request-id"),
                assertionWithOnlyFirstName(REQUEST_ID)
            ), REQUEST_ID, HUB_ENTITY_ID))
            .build();


        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATE_FAILURE);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    private List<Attribute> attributesFromUacAttributes(Stream<UserAccountCreationAttribute> uacAttributes) {
        return uacAttributes
                .map(userAccountCreationAttribute -> new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());
    }

    protected Attribute userAccountCreationAttributeFor(AttributeValue attributeValue, UserAccountCreationAttribute userAccountCreationAttribute) {
        return aUserAccountCreationAttributeValue().addValue(attributeValue).buildAsAttribute(userAccountCreationAttribute);
    }

    protected Attribute userAccountCreationAttributeFor(List<AttributeValue> attributeValues, UserAccountCreationAttribute userAccountCreationAttribute) {
        UserAccountCreationValueAttributeBuilder attributeBuilder = aUserAccountCreationAttributeValue();
        for(AttributeValue attributeValue : attributeValues) {
            attributeBuilder.addValue(attributeValue);
        }
        return attributeBuilder.buildAsAttribute(userAccountCreationAttribute);
    }
}
