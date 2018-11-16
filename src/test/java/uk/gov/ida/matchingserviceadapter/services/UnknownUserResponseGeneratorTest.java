package uk.gov.ida.matchingserviceadapter.services;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.UnknownUserCreationIdaStatus;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aCurrentAddress;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aHistoricalAddress;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.ADDRESS_HISTORY;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CURRENT_ADDRESS_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CYCLE_3;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.DATE_OF_BIRTH;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.DATE_OF_BIRTH_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME_VERIFIED;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.FAILURE;
import static uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto.SUCCESS;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

@RunWith(OpenSAMLMockitoRunner.class)
public class UnknownUserResponseGeneratorTest {

    private static final String REQUEST_ID = "requestId";
    private static final String HASHED_PID = "hashedPid";
    private static final String ASSERTION_CONSUMER_SERVICE_URL = "assertionConsumerServiceUrl";
    private static final String AUTHN_REQUEST_ISSUER_ID = "authnRequestIssuerId";
    private static final String ENTITY_ID = "entityId";
    private static final String TEST_ID = "testId";
    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    private UnknownUserResponseGenerator unknownUserResponseGenerator;

    private String hashedPid = "hashedPid";
    private String issuerId = "some-idp";
    private String nameId = "nameId";
    private AuthnContext authnContext = AuthnContext.LEVEL_2;
    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    @Before
    public void setup() {
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.days(2));
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
        when(idGenerator.getId()).thenReturn(TEST_ID);

        unknownUserResponseGenerator = new UnknownUserResponseGenerator(
            configuration,
            assertionLifetimeConfiguration,
            new UserAccountCreationAttributeExtractor(),
            idGenerator);

        DateTimeFreezer.freezeTime();
    }

    @After
    public void tearDown() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void shouldReturnSuccessResponseWhenMatchingServiceReturnsSuccess() {
        LocalDate dob = new LocalDate(1970, 1, 2);
        LocalDate oldDob = new LocalDate(1970, 2, 1);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").withVerifiedStatus(true).build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Bob").build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").withVerifiedStatus(true).build())
                .addSurname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Smith").build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aHistoricalSimpleMdsValue().withValue(oldDob).build())
                .withCurrentAddresses(asList(aCurrentAddress().withPostCode("AA12BB").build()))
                .withPreviousAddresses(asList(aHistoricalAddress().withPostCode("CC12DD").build()))
                .withGender(SimpleMdsValueBuilder.<Gender>aCurrentSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build())
                .build();
        AssertionData assertionData = new AssertionData(
                "an-mds-issuer",
                AuthnContext.LEVEL_2,
                Optional.of(Cycle3Dataset.createFromData("NI", "123456")),
                matchingDataset
        );

        List<Attribute> requestedUserAccountCreationAttributes = asList(
                createAttribute(FIRST_NAME),
                createAttribute(FIRST_NAME_VERIFIED),
                createAttribute(MIDDLE_NAME),
                createAttribute(MIDDLE_NAME_VERIFIED),
                createAttribute(SURNAME),
                createAttribute(SURNAME_VERIFIED),
                createAttribute(DATE_OF_BIRTH),
                createAttribute(DATE_OF_BIRTH_VERIFIED),
                createAttribute(CURRENT_ADDRESS),
                createAttribute(CURRENT_ADDRESS_VERIFIED),
                createAttribute(ADDRESS_HISTORY),
                createAttribute(CYCLE_3));

        UnknownUserCreationResponseDto unknownUserCreationResponseDto = new UnknownUserCreationResponseDto(SUCCESS);

        OutboundResponseFromUnknownUserCreationService response = unknownUserResponseGenerator.getMatchingServiceResponse(unknownUserCreationResponseDto,
                REQUEST_ID,
                HASHED_PID,
                ASSERTION_CONSUMER_SERVICE_URL,
                AUTHN_REQUEST_ISSUER_ID,
                assertionData,
                requestedUserAccountCreationAttributes);

        assertThat(response.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.Success);

        // Check correct IDs are set on Response object
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getId()).isEqualTo(TEST_ID);
        assertThat(response.getIssuer()).isEqualTo(ENTITY_ID);

        // Check correct IDs/lifetimes are set on Assertion
        assertThat(response.getMatchingServiceAssertion()).isPresent();
        assertThat(response.getMatchingServiceAssertion().get().getAssertionRestrictions()).isNotNull();
        assertThat(response.getMatchingServiceAssertion().get().getId()).isEqualTo(TEST_ID);
        assertThat(response.getMatchingServiceAssertion().get().getIssuerId()).isEqualTo(ENTITY_ID);
        assertThat(response.getMatchingServiceAssertion().get().getAssertionRestrictions().getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getMatchingServiceAssertion().get().getAssertionRestrictions().getNotOnOrAfter()).isEqualTo(DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()));
        assertThat(response.getMatchingServiceAssertion().get().getAssertionRestrictions().getRecipient()).isEqualTo(ASSERTION_CONSUMER_SERVICE_URL);

        Map<String, String> expectedValues = new HashMap<String, String>();
        expectedValues.put(FIRST_NAME.getAttributeName(), "Joe");
        expectedValues.put(SURNAME.getAttributeName(), "Bloggs");
        expectedValues.put(DATE_OF_BIRTH.getAttributeName(), dob.toString());
        expectedValues.put(CYCLE_3.getAttributeName(), "123456");

        // Check Assertion contains expected number/type of User Account Creation attributes
        List<Attribute> userAttributesForAccountCreation = response.getMatchingServiceAssertion().get().getUserAttributesForAccountCreation();
        Map<Boolean, List<Attribute>> attributeMap = userAttributesForAccountCreation.stream()
                .collect(Collectors.groupingBy(this::isAddressHistory));

        attributeMap.get(false).forEach(a -> assertThat(a.getAttributeValues().size()).isEqualTo(1));
        assertThat(attributeMap.get(true).get(0).getAttributeValues().size()).isEqualTo(2);

        userAttributesForAccountCreation.stream()
                .filter(a -> expectedValues.containsKey(a.getName()))
                .forEach(a -> {
            String attributeValue = ((StringValueSamlObject) a.getAttributeValues().get(0)).getValue();
            assertThat(expectedValues.get(a.getName())).isEqualTo(attributeValue);
        });
    }

    @Test
    public void shouldReturnFailureResponseWhenMatchingServiceReturnsFailure() {
        UnknownUserCreationResponseDto unknownUserCreationResponseDto = new UnknownUserCreationResponseDto(FAILURE);

        OutboundResponseFromUnknownUserCreationService response = unknownUserResponseGenerator.getMatchingServiceResponse(unknownUserCreationResponseDto,
                REQUEST_ID,
                HASHED_PID,
                ASSERTION_CONSUMER_SERVICE_URL,
                AUTHN_REQUEST_ISSUER_ID,
                mock(AssertionData.class),
                Collections.emptyList());

        assertThat(response.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.CreateFailure);
        // Check correct IDs are set on Response object
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getId()).isEqualTo(TEST_ID);
        assertThat(response.getIssuer()).isEqualTo(ENTITY_ID);
    }

    @Test
    public void shouldReturnAttributeFailureResponseWhenNoAttributesExist() {
        UnknownUserCreationResponseDto unknownUserCreationResponseDto = new UnknownUserCreationResponseDto(SUCCESS);
        MatchingDataset matchingDataset = aMatchingDataset().addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").build()).build();
        AssertionData assertionData = mock(AssertionData.class);
        when(assertionData.getMatchingDataset()).thenReturn(matchingDataset);
        List<Attribute> requestedUserAccountCreationAttributes = asList(createAttribute(SURNAME));

        OutboundResponseFromUnknownUserCreationService response = unknownUserResponseGenerator.getMatchingServiceResponse(unknownUserCreationResponseDto,
                REQUEST_ID,
                HASHED_PID,
                ASSERTION_CONSUMER_SERVICE_URL,
                AUTHN_REQUEST_ISSUER_ID,
                assertionData,
                requestedUserAccountCreationAttributes);

        assertThat(response.getStatus()).isEqualTo(UnknownUserCreationIdaStatus.NoAttributeFailure);
        // Check correct IDs are set on Response object
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getId()).isEqualTo(TEST_ID);
        assertThat(response.getIssuer()).isEqualTo(ENTITY_ID);
    }

    private Attribute createAttribute(UserAccountCreationAttribute userAccountCreationAttribute) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();

        String attributeName = userAccountCreationAttribute.getAttributeName();
        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeName);
        attribute.setNameFormat(Attribute.UNSPECIFIED);
        return attribute;
    }

    private boolean isAddressHistory(Attribute attribute) {
        return attribute.getName().equals(ADDRESS_HISTORY.getAttributeName());
    }
}
