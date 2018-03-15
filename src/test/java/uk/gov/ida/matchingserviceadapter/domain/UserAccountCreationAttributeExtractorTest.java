package uk.gov.ida.matchingserviceadapter.domain;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.builders.MatchingDatasetBuilder;
import uk.gov.ida.matchingserviceadapter.factories.AttributeQueryAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.PersistentId;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.extensions.StringBasedMdsAttributeValue;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.saml.core.extensions.impl.PersonNameImpl;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.ADDRESS_HISTORY;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.CYCLE_3;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.SURNAME_VERIFIED;

@RunWith(OpenSAMLRunner.class)
public class UserAccountCreationAttributeExtractorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final AttributeQueryAttributeFactory attributeQueryAttributeFactory1 = new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory());
    private final AttributeQueryAttributeFactory attributeQueryAttributeFactory = attributeQueryAttributeFactory1;
    private UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor = new UserAccountCreationAttributeExtractor();

    @Test
    public void shouldReturnCurrentSurnamesWhenMatchingDatasetHasListOfSurnames() {
        List<Attribute> accountCreationAttributes = singletonList(SURNAME).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, true);
        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", new DateTime(2000, 1, 30, 0, 0), new DateTime(2010, 1, 30, 0, 0), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", new DateTime(1990, 1, 30, 0, 0), new DateTime(2000, 1, 29, 0, 0), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2, currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames.size()).isEqualTo(1);
        assertThat(personName.getValue().equals("CurrentSurname"));
    }

    @Test
    public void shouldReturnCurrentAddressWhenMatchingDatasetHasListOfAddresses() {
        List<Attribute> accountCreationAttributes = singletonList(UserAccountCreationAttribute.CURRENT_ADDRESS).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, true);
        Address oldAddress = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode", "old_internationalPostCode", "old_uprn", new DateTime(1990, 1, 30, 0, 0), new DateTime(2000, 1, 29, 0, 0), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).withPreviousAddresses(Arrays.asList(oldAddress)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> addresses = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("currentaddress")).collect(toList());
        AddressImpl addressName = (AddressImpl) addresses.get(0).getAttributeValues().get(0);

        assertThat(addresses.size()).isEqualTo(1);
        assertThat(addressName.getPostCode().equals("postCode"));
    }

    @Test
    public void shouldReturnCurrentSurnameWhetherItIsVerifiedOrNot() {
        List<Attribute> accountCreationAttributes = singletonList(SURNAME).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, false);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(singletonList(currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames.size()).isEqualTo(1);
        assertThat(personName.getValue().equals("CurrentSurname"));
    }

    @Test
    public void shouldReturnCurrentAddressWhetherItIsVerifiedOrNot() {
        List<Attribute> accountCreationAttributes = singletonList(UserAccountCreationAttribute.CURRENT_ADDRESS).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, false);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> addresses = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("currentaddress")).collect(toList());
        AddressImpl addressName = (AddressImpl) addresses.get(0).getAttributeValues().get(0);

        assertThat(addresses.size()).isEqualTo(1);
        assertThat(addressName.getPostCode().equals("postCode"));
    }

    @Test
    public void shouldNotReturnAttributesWhenNoCurrentValueExistsInMatchingDataSet() {
        List<Attribute> accountCreationAttributes = singletonList(SURNAME).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", new DateTime(2000, 1, 30, 0, 0), new DateTime(2010, 1, 30, 0, 0), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", new DateTime(1990, 1, 30, 0, 0), new DateTime(2000, 1, 29, 0, 0), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());

        assertThat(surnames.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnVerifiedIfAllCurrentAttributeValuesAreVerified() throws Exception {
        List<Attribute> accountCreationAttributes = Arrays.asList(SURNAME, SURNAME_VERIFIED).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());


        SimpleMdsValue<String> currentSurname = new SimpleMdsValue<>("CurrentSurname", null, null, true);
        SimpleMdsValue<String> oldSurname1 = new SimpleMdsValue<>("OldSurname1", new DateTime(2000, 1, 30, 0, 0), new DateTime(2010, 1, 30, 0, 0), true);
        SimpleMdsValue<String> oldSurname2 = new SimpleMdsValue<>("OldSurname2", new DateTime(1990, 1, 30, 0, 0), new DateTime(2000, 1, 29, 0, 0), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withSurnameHistory(Arrays.asList(oldSurname1, oldSurname2, currentSurname)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> surnames = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());
        List<Attribute>  verified = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname_verified")).collect(toList());
        PersonNameImpl personName = (PersonNameImpl) surnames.get(0).getAttributeValues().get(0);

        assertThat(surnames.size()).isEqualTo(1);
        assertThat(personName.getValue().equals("CurrentSurname"));
        assertThat(verified.size()).isEqualTo(1);
        assertThat(verified.get(0).getName().equals("surname_verified"));
    }

    @Test
    public void shouldReturnFullAddressHistoryIncludingWhetherTheyAreVerified() throws Exception {
        List<Attribute> accountCreationAttributes = Arrays.asList(ADDRESS_HISTORY).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        Address currentAddress = new Address(Arrays.asList("line1", "line2", "line3"), "postCode", "internationalPostCode", "uprn", null, null, true);
        Address oldAddress = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode", "old_internationalPostCode", "old_uprn", new DateTime(1990, 1, 30, 0, 0), new DateTime(2000, 1, 29, 0, 0), false);
        Address oldAddress2 = new Address(Arrays.asList("old_line1", "old_line2", "old_line3"), "old_postCode_2", "old_internationalPostCode_2", "old_uprn", new DateTime(2000, 1, 30, 0, 0), new DateTime(2010, 1, 29, 0, 0), true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().withCurrentAddresses(singletonList(currentAddress)).withPreviousAddresses(Arrays.asList(oldAddress, oldAddress2)).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        Attribute addressHistoryAttribute = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("addresshistory")).collect(toList()).get(0);
        List<AddressImpl> addresses = addressHistoryAttribute.getAttributeValues().stream().map(v -> (AddressImpl) v).collect(toList());

        AddressImpl firstAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("postCode")).findFirst().get();
        AddressImpl secondAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("old_postCode")).findFirst().get();
        AddressImpl thirdAddress = addresses.stream().filter(a -> a.getPostCode().getValue().equals("old_postCode_2")).findFirst().get();

        assertThat(addresses.size()).isEqualTo(3);
        assertThat(firstAddress.getVerified()).isTrue();
        assertThat(secondAddress.getVerified()).isFalse();
        assertThat(thirdAddress.getVerified()).isTrue();
    }

    @Test
    public void shouldReturnRequiredCycle3AttributesWhenValuesExistInCycle3Assertion(){
        List<Attribute> accountCreationAttributes = Arrays.asList(CYCLE_3).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        ImmutableMap<String, String> build = ImmutableMap.<String, String>builder().put("cycle3Key", "cycle3Value").build();
        Cycle3Dataset cycle3Dataset = Cycle3Dataset.createFromData(build);
        HubAssertion hubAssertion =new HubAssertion("1", "issuerId", DateTime.now(), new PersistentId("1"), null, Optional.of(cycle3Dataset));

        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, null, Optional.of(hubAssertion));

        List<Attribute> cycle_3 = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("cycle_3")).collect(toList());
        StringBasedMdsAttributeValue personName = (StringBasedMdsAttributeValue) cycle_3.get(0).getAttributeValues().get(0);

        assertThat(cycle_3.size()).isEqualTo(1);
        assertThat(personName.getValue().equals("cycle3Value"));
    }

    @Test
    public void shouldReturnRequiredAttributesForAccountCreationWithoutAttributeWhichIsMissingInMatchingDataSet(){
        List<Attribute> accountCreationAttributes = Arrays.asList(SURNAME, FIRST_NAME).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        SimpleMdsValue<String> surname = new SimpleMdsValue<>("CurrentSurname", null, null, true);

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().addSurname(surname).build();
        List<Attribute> userAttributesForAccountCreation = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.of(matchingDataset), Optional.empty());

        List<Attribute> attributes = userAttributesForAccountCreation.stream().filter(a -> a.getName().equals("surname")).collect(toList());

        assertThat(attributes.size()).isEqualTo(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void willThrowExceptionIfWeHaveNoMatchingDataset(){
    //Added this test to highlight the dangerous use of .get on Optional.

        List<Attribute> accountCreationAttributes = Arrays.asList(FIRST_NAME).stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(toList());

        userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(accountCreationAttributes, Optional.empty(), Optional.empty());
    }
}