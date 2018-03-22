package uk.gov.ida.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import uk.gov.ida.Constants;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder;
import uk.gov.ida.integrationtest.helpers.AssertionHelper;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.matchingserviceadapter.factories.AttributeQueryAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.saml.core.extensions.impl.VerifiedImpl;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.builders.AddressAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.RESPONDER;
import static org.opensaml.saml.saml2.core.StatusCode.SUCCESS;
import static uk.gov.ida.integrationtest.builders.UserAccountCreationValueAttributeBuilder.aUserAccountCreationAttributeValue;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.ADDRESS_HISTORY;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CYCLE_3;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME_VERIFIED;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.CREATED;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.CREATE_FAILURE;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static uk.gov.ida.saml.core.test.builders.VerifiedAttributeValueBuilder.aVerifiedValue;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;

public class UserAccountCreationAppRuleTest {
    private static final String METADATA_PATH = "/uk/gov/ida/saml/metadata/sp";
    private static final String MATCHING_REQUEST_PATH = "/matching-request";
    private static final String UNKNOWN_USER_MATCHING_PATH = "/unknown-user-attribute-query";
    private static final String REQUEST_ID = "default-request-id";

    @ClassRule
    public static final HttpStubRule localMatchingService = new HttpStubRule();
    @ClassRule
    public static final HttpStubRule metadataServer = new HttpStubRule();

    static {
        //doALittleHackToMakeGuicierHappy
        // magically, this has to be the first test to run otherwise things will fail.
        // see:
        // - https://github.com/HubSpot/dropwizard-guice/issues/95
        // - https://github.com/Squarespace/jersey2-guice/pull/39
        JerseyGuiceUtils.reset();

        try {
            InitializationService.initialize();

            metadataServer.reset();
            metadataServer.register(METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> applicationRule = new MatchingServiceAdapterAppRule(
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH),
        ConfigOverride.config("localMatchingService.accountCreationUrl", "http://localhost:" + localMatchingService.getPort() + UNKNOWN_USER_MATCHING_PATH)
    );

    private final String UNKNOWN_USER_URI = "http://localhost:" + applicationRule.getLocalPort() + "/unknown-user-attribute-query";
    private AssertionDecrypter assertionDecrypter;
    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    @Before
    public void setup() throws Exception {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));

        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        assertionDecrypter = new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);
        setUpMatchingService();
    }

    private void setUpMatchingService() throws Exception {
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"success\"}");
    }

    @Test
    public void shouldReturnCurrentAttributesWhenPassedFullMatchingDataset() throws Exception {
        List<Attribute> requiredAttributes = Stream.of(FIRST_NAME, FIRST_NAME_VERIFIED, MIDDLE_NAME, MIDDLE_NAME_VERIFIED, SURNAME, SURNAME_VERIFIED, CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY, CYCLE_3)
                .map(userAccountCreationAttribute -> new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());

        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(applicationRule.getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        aCompleteMatchingDatasetAssertion(),
                        AssertionBuilder.aCycle3DatasetAssertion("cycle3Name", "cycle3Value")), REQUEST_ID, HUB_ENTITY_ID))
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
                        Arrays.asList(
                                anAddressAttributeValue().addLines(ImmutableList.of("address line 1")).withVerified(false).build(),
                                anAddressAttributeValue().addLines(ImmutableList.of("address line 2")).withVerified(true).build()
                        ),
                        ADDRESS_HISTORY),
                userAccountCreationAttributeFor(openSamlXmlObjectFactory.createSimpleMdsAttributeValue("cycle3Value"), CYCLE_3)
                ));
        Assertions.assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        Assertions.assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }


    @Test
    public void shouldReturnFailureResponseWhenAttributesRequestedDoNotExist(){
        List<Attribute> requiredAttributes = asList(FIRST_NAME, MIDDLE_NAME).stream()
                .map(userAccountCreationAttribute -> new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());

        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(applicationRule.getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        assertionWithOnlyFirstName()), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        Assertions.assertThat(decryptedAssertions).hasSize(0);
        Assertions.assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        Assertions.assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnResponderStatusCodeWhenLocalMatchingServiceIsDown() throws JsonProcessingException {
        localMatchingService.reset();
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"failure\"}");

        List<Attribute> requiredAttributes = singletonList(new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(FIRST_NAME));

        AttributeQuery attributeQuery = anAttributeQuery()
                .withId(REQUEST_ID)
                .withAttributes(requiredAttributes)
                .withIssuer(anIssuer().withIssuerId(applicationRule.getConfiguration().getHubEntityId()).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        assertionWithOnlyFirstName()
                        ), REQUEST_ID, HUB_ENTITY_ID))
                .build();


        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(CREATE_FAILURE);
        Assertions.assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        Assertions.assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));

    }

    private Attribute userAccountCreationAttributeFor(AttributeValue attributeValue, UserAccountCreationAttribute userAccountCreationAttribute) {
        return aUserAccountCreationAttributeValue().addValue(attributeValue).buildAsAttribute(userAccountCreationAttribute);
    }

    private Attribute userAccountCreationAttributeFor(List<AttributeValue> attributeValues, UserAccountCreationAttribute userAccountCreationAttribute) {
        UserAccountCreationValueAttributeBuilder attributeBuilder = aUserAccountCreationAttributeValue();
        for(AttributeValue attributeValue : attributeValues) {
            attributeBuilder.addValue(attributeValue);
        }
        return attributeBuilder.buildAsAttribute(userAccountCreationAttribute);
    }

    private void assertThatResponseContainsExpectedUserCreationAttributes(List<AttributeStatement>  attributeStatements, final List<Attribute> expectedUserCreationAttributes) {
        assertThat(attributeStatements).hasSize(1);
        AttributeStatement attributeStatement = attributeStatements.get(0);
        assertThat(attributeStatement.getAttributes()).hasSameSizeAs(expectedUserCreationAttributes);

        for (final Attribute expectedUserCreationAttribute : expectedUserCreationAttributes) {
            Attribute actualAttribute = attributeStatement.getAttributes().stream()
                    .filter(attribute -> expectedUserCreationAttribute.getName().equals(attribute.getName()))
                    .findFirst()
                    .get();

            assertThat(actualAttribute.getAttributeValues()).hasSameSizeAs(expectedUserCreationAttribute.getAttributeValues());
            if (!actualAttribute.getAttributeValues().isEmpty()) {
                assertThatAttributeValuesAreEqual(actualAttribute.getAttributeValues().get(0), expectedUserCreationAttribute.getAttributeValues().get(0));
            }
        }
    }

    private void assertThatAddressAttributeValuesAreEqual(AddressImpl actualValue, AddressImpl expectedValue) {
        assertThat(actualValue.getLines()).hasSameSizeAs(expectedValue.getLines());
        for (int i = 0; i < actualValue.getLines().size(); i++) {
            assertThat((actualValue.getLines().get(i).getValue())).isEqualTo(expectedValue.getLines().get(i).getValue());
        }

        assertThat(actualValue.getPostCode().getValue()).isEqualTo(expectedValue.getPostCode().getValue());
        assertThat(actualValue.getInternationalPostCode().getValue()).isEqualTo(expectedValue.getInternationalPostCode().getValue());
        assertThat(actualValue.getUPRN().getValue()).isEqualTo(expectedValue.getUPRN().getValue());
        assertThat(actualValue.getFrom()).isEqualTo(expectedValue.getFrom());
        assertThat(actualValue.getTo()).isEqualTo(expectedValue.getTo());
    }


    private void assertThatAttributeValuesAreEqual(XMLObject actualValue, XMLObject expectedValue) {
        if (actualValue instanceof AddressImpl) {
            assertThatAddressAttributeValuesAreEqual((AddressImpl) actualValue, (AddressImpl) expectedValue);
        } else if (actualValue instanceof VerifiedImpl) {
            Verified actualAttributeValue = (Verified) actualValue;
            Verified expectedAttributeValue = (Verified) expectedValue;
            assertThat(actualAttributeValue.getValue()).isEqualTo(expectedAttributeValue.getValue());
        } else {
            StringValueSamlObject actualAttributeValue = (StringValueSamlObject) actualValue;
            StringValueSamlObject expectedAttributeValue = (StringValueSamlObject) expectedValue;
            assertThat(actualAttributeValue.getValue()).isEqualTo(expectedAttributeValue.getValue());
        }
    }

    private Assertion aCompleteMatchingDatasetAssertion() {
        return aMatchingDatasetAssertion(asList(
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldSurname").withFrom(new DateTime(1990, 1, 30, 0, 0)).withTo(new DateTime(2000, 1, 29, 0, 0)).withVerified(true).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("CurrentSurname").withVerified(true).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("FirstName").withVerified(false).build()).buildAsFirstname(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(new AddressAttributeValueBuilder_1_1().addLines(ImmutableList.of("address line 1")).withVerified(false).build()).buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(new AddressAttributeValueBuilder_1_1().addLines(ImmutableList.of("address line 2")).withVerified(true).build()).buildPreviousAddress(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth()));
    }

    private Assertion assertionWithOnlyFirstName() {
        return aMatchingDatasetAssertion(asList(
                aPersonName_1_1().addValue(aPersonNameValue().withValue("SteveFirstName").withFrom(new DateTime(2000, 1, 30, 0, 0)).withTo(new DateTime(2010, 12, 30, 0, 0)).build()).buildAsFirstname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("Surname").build()).buildAsSurname()));
    }

    private Assertion aMatchingDatasetAssertion(List<Attribute> attributes) {
        return AssertionHelper.aMatchingDatasetAssertion(attributes, false, REQUEST_ID);
    }

}
