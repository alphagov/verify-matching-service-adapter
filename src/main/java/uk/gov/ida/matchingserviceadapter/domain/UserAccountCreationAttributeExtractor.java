package uk.gov.ida.matchingserviceadapter.domain;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.joda.time.LocalDate;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.saml.factories.UserAccountCreationAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class UserAccountCreationAttributeExtractor {

    @Inject
    public UserAccountCreationAttributeExtractor() {}

    private final UserAccountCreationAttributeFactory userAccountCreationAttributeFactory = new UserAccountCreationAttributeFactory(new OpenSamlXmlObjectFactory());

    public List<Attribute> getUserAttributesForAccountCreation(List<Attribute> userCreationAttributes, Optional<MatchingDataset> optionalMatchingDataset, Optional<HubAssertion> cycle3Data) {
        List<Attribute> userCreationAttributesWithValues = new ArrayList<>();

        for (Attribute userCreationAttribute : userCreationAttributes) {
            UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.getUserAccountCreationAttribute(userCreationAttribute.getName());

            switch(userAccountCreationAttribute){
                case FIRST_NAME:

                    //How can we be sure that the Matching dataset will not be absent, and if we can be sure then why is it optional? (This happens in each case)
                    MatchingDataset matchingDataset = optionalMatchingDataset.get();

                    List<SimpleMdsValue<String>> firstNameAttributeValues = getAttributeValuesWithoutMdsDetails(matchingDataset.getFirstNames(), userAccountCreationAttribute);
                    if(!firstNameAttributeValues.isEmpty()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationFirstnameAttribute(firstNameAttributeValues));
                    }
                    break;
                case FIRST_NAME_VERIFIED:
                    Optional<SimpleMdsValue<String>> currentFirstName = getCurrentValue(optionalMatchingDataset.get().getFirstNames(), userAccountCreationAttribute);
                    if (currentFirstName.isPresent()) {
                        userCreationAttributesWithValues.add(
                                userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.FIRST_NAME_VERIFIED, currentFirstName.get().isVerified())
                        );
                    }
                    break;
                case MIDDLE_NAME:
                    List<SimpleMdsValue<String>> middleNameAttributeValues = getAttributeValuesWithoutMdsDetails(optionalMatchingDataset.get().getMiddleNames(), userAccountCreationAttribute);
                    if(!middleNameAttributeValues.isEmpty()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationMiddlenameAttribute(middleNameAttributeValues));
                    }
                    break;
                case MIDDLE_NAME_VERIFIED:
                    Optional<SimpleMdsValue<String>> currentMiddleName = getCurrentValue(optionalMatchingDataset.get().getMiddleNames(), userAccountCreationAttribute);
                    if (currentMiddleName.isPresent()) {
                        userCreationAttributesWithValues.add(
                                userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED, currentMiddleName.get().isVerified()));
                    }
                    break;
                case SURNAME:
                    List<SimpleMdsValue<String>> surnameAttributeValues = getAttributeValuesWithoutMdsDetails(optionalMatchingDataset.get().getSurnames(), userAccountCreationAttribute);
                    if(!surnameAttributeValues.isEmpty()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationSurnameAttribute(surnameAttributeValues));
                    }
                    break;
                case SURNAME_VERIFIED:
                    Optional<SimpleMdsValue<String>> currentSurname = getCurrentValue(optionalMatchingDataset.get().getSurnames(), userAccountCreationAttribute);
                    if (currentSurname.isPresent()) {
                        userCreationAttributesWithValues.add(
                                userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.SURNAME_VERIFIED, currentSurname.get().isVerified())
                        );
                    }
                    break;
                case DATE_OF_BIRTH:
                    List<SimpleMdsValue<LocalDate>> dateOfBirthAttributeValues = getAttributeValuesWithoutMdsDetails(optionalMatchingDataset.get().getDateOfBirths(), userAccountCreationAttribute);
                    if(!dateOfBirthAttributeValues.isEmpty()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationDateOfBirthAttribute(dateOfBirthAttributeValues));
                    }
                    break;
                case DATE_OF_BIRTH_VERIFIED:
                    Optional<SimpleMdsValue<LocalDate>> currentDob = getCurrentValue(optionalMatchingDataset.get().getDateOfBirths(), userAccountCreationAttribute);
                    if (currentDob.isPresent()) {
                        userCreationAttributesWithValues.add(
                                userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.DATE_OF_BIRTH_VERIFIED, currentDob.get().isVerified())
                        );
                    }
                    break;
                case CURRENT_ADDRESS:
                    Optional<Address> currentAddresses = extractCurrentAddress(optionalMatchingDataset.get().getCurrentAddresses(), userAccountCreationAttribute);
                    if(currentAddresses.isPresent()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationCurrentAddressAttribute(ImmutableList.of(currentAddresses.get())));
                    }
                    break;
                case CURRENT_ADDRESS_VERIFIED:
                    Optional<Address> currentAddress = extractCurrentAddress(optionalMatchingDataset.get().getCurrentAddresses(), userAccountCreationAttribute);
                    if(currentAddress.isPresent()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.CURRENT_ADDRESS_VERIFIED, currentAddress.get().isVerified()));
                    }
                    break;
                case ADDRESS_HISTORY:
                    List<Address> allAddresses = optionalMatchingDataset.get().getAddresses();
                    if(!allAddresses.isEmpty()) {
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationAddressHistoryAttribute(ImmutableList.copyOf(allAddresses)));
                    }
                    break;
                case CYCLE_3:
                    if (cycle3Data.isPresent()) {
                        Collection<String> cycle3Attributes = cycle3Data.get().getCycle3Data().get().getAttributes().values();
                        userCreationAttributesWithValues.add(userAccountCreationAttributeFactory.createUserAccountCreationCycle3DataAttributes(cycle3Attributes));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();

            }
        }

        return userCreationAttributesWithValues;
    }

    private <T> List<SimpleMdsValue<T>> getAttributeValuesWithoutMdsDetails(final List<SimpleMdsValue<T>> simpleMdsValues, UserAccountCreationAttribute userAccountCreationAttribute) {
        Optional<SimpleMdsValue<T>> currentValue = getCurrentValue(simpleMdsValues, userAccountCreationAttribute);
        List<SimpleMdsValue<T>> attributesWithoutMdsDetails = new ArrayList<>();
        if (currentValue.isPresent()) {
            attributesWithoutMdsDetails.add(new SimpleMdsValue<>(currentValue.get().getValue(), null, null, false));
        }
        return attributesWithoutMdsDetails;
    }

    private Optional<Address> extractCurrentAddress(List<Address> addresses, UserAccountCreationAttribute userAccountCreationAttribute) {
        List<Address> currentValues = ImmutableList.copyOf(addresses.stream()
                .filter(candidateValue -> !candidateValue.getTo().isPresent())
                .collect(toList()));

        if (currentValues.size() > 1) {
            String message = MessageFormat.format("There cannot be multiple current values for {0} attribute.", userAccountCreationAttribute.getAttributeName());
            throw new WebApplicationException(new IllegalStateException(message), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (currentValues.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(currentValues.get(0));
    }

    private <T> Optional<SimpleMdsValue<T>> getCurrentValue(final List<SimpleMdsValue<T>> simpleMdsValues, UserAccountCreationAttribute userAccountCreationAttribute) {
        List<SimpleMdsValue<T>> currentValues = ImmutableList.copyOf(simpleMdsValues.stream()
                .filter(simpleMdsValue -> simpleMdsValue.getTo() == null)
                .collect(toList()));
        if (currentValues.size() > 1) {
            String message = MessageFormat.format("There cannot be multiple current values for {0} attribute.", userAccountCreationAttribute.getAttributeName());
            throw new WebApplicationException(new IllegalStateException(message), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (currentValues.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(currentValues.get(0));
    }

}
