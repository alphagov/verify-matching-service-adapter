package uk.gov.ida.matchingserviceadapter.domain;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalDate;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.saml.factories.UserAccountCreationAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public enum UserAccountCreationAttribute implements Serializable, AttributeExtractor {
    FIRST_NAME("firstname"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<SimpleMdsValue<String>> firstNameAttributeValues =
                    getAttributeValuesWithoutMdsDetails(matchingDataset.getFirstNames());
            return optionalOfList(
                    firstNameAttributeValues,
                    userAccountCreationAttributeFactory.createUserAccountCreationFirstnameAttribute(firstNameAttributeValues)
            );
        }
    },
    FIRST_NAME_VERIFIED("firstname_verified"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getFirstNames())
                    .map((SimpleMdsValue<String> stringSimpleMdsValue) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.FIRST_NAME_VERIFIED,
                                    stringSimpleMdsValue.isVerified()
                            )
                    );
        }
    },
    MIDDLE_NAME("middlename"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<SimpleMdsValue<String>> middleNameAttributeValues = getAttributeValuesWithoutMdsDetails(matchingDataset.getMiddleNames());
            return optionalOfList(
                    middleNameAttributeValues,
                    userAccountCreationAttributeFactory.createUserAccountCreationMiddlenameAttribute(middleNameAttributeValues)
            );
        }
    },
    MIDDLE_NAME_VERIFIED("middlename_verified"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getMiddleNames())
                    .map((SimpleMdsValue<String> stringSimpleMdsValue) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED,
                                    stringSimpleMdsValue.isVerified()
                            )
                    );
        }
    },
    SURNAME("surname"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<SimpleMdsValue<String>> surnameAttributeValues = getAttributeValuesWithoutMdsDetails(matchingDataset.getSurnames());
            return optionalOfList(
                    surnameAttributeValues,
                    userAccountCreationAttributeFactory.createUserAccountCreationSurnameAttribute(surnameAttributeValues)
            );
        }
    },
    SURNAME_VERIFIED("surname_verified"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getSurnames())
                    .map((SimpleMdsValue<String> stringSimpleMdsValue) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.SURNAME_VERIFIED,
                                    stringSimpleMdsValue.isVerified()
                            )
                    );
        }
    },
    DATE_OF_BIRTH("dateofbirth"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<SimpleMdsValue<LocalDate>> dateOfBirthAttributeValues = getAttributeValuesWithoutMdsDetails(matchingDataset.getDateOfBirths());
            return optionalOfList(
                    dateOfBirthAttributeValues,
                    userAccountCreationAttributeFactory.createUserAccountCreationDateOfBirthAttribute(dateOfBirthAttributeValues));
        }
    },
    DATE_OF_BIRTH_VERIFIED("dateofbirth_verified"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getDateOfBirths())
                    .map((SimpleMdsValue<LocalDate> localDateSimpleMdsValue) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.DATE_OF_BIRTH_VERIFIED,
                                    localDateSimpleMdsValue.isVerified()
                            )
                    );
        }
    },
    CURRENT_ADDRESS("currentaddress"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return extractCurrentAddress(matchingDataset.getCurrentAddresses()).
                    map((Address address) -> userAccountCreationAttributeFactory.createUserAccountCreationCurrentAddressAttribute(ImmutableList.of(address)));
        }
    },
    CURRENT_ADDRESS_VERIFIED("currentaddress_verified"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return extractCurrentAddress(matchingDataset.getCurrentAddresses())
                    .map((Address address) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.CURRENT_ADDRESS_VERIFIED,
                                    address.isVerified()
                            )
                    );
        }
    },
    ADDRESS_HISTORY("addresshistory"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<Address> allAddresses = matchingDataset.getAddresses();
            return optionalOfList(
                    allAddresses,
                    userAccountCreationAttributeFactory.createUserAccountCreationAddressHistoryAttribute(ImmutableList.copyOf(allAddresses)));
        }
    },
    CYCLE_3("cycle_3"){
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return cycle3Dataset
                    .map(c3 -> c3.getAttributes().values())
                    .map(userAccountCreationAttributeFactory::createUserAccountCreationCycle3DataAttributes);
        }
    };

    private static final UserAccountCreationAttributeFactory userAccountCreationAttributeFactory = new UserAccountCreationAttributeFactory(new OpenSamlXmlObjectFactory());

    private String attributeName;

    UserAccountCreationAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName(){
        return attributeName;
    }

    public static UserAccountCreationAttribute getUserAccountCreationAttribute(final String name){
        return Arrays.stream(values())
                .filter(value -> value.getAttributeName().equals(name))
                .findFirst()
                .get();
    }

    private static <T> Optional<Attribute> optionalOfList(List<T> firstNameAttributeValues, Attribute userAccountCreationFirstnameAttribute) {
        return !firstNameAttributeValues.isEmpty() ? Optional.of(userAccountCreationFirstnameAttribute) : Optional.empty();
    }

    private static <T> List<SimpleMdsValue<T>> getAttributeValuesWithoutMdsDetails(final List<SimpleMdsValue<T>> simpleMdsValues) {
        List<SimpleMdsValue<T>> attributesWithoutMdsDetails = new ArrayList<>();
        getCurrentValue(simpleMdsValues).ifPresent((SimpleMdsValue<T> tSimpleMdsValue) ->
                attributesWithoutMdsDetails.add(new SimpleMdsValue<>(tSimpleMdsValue.getValue(), null, null, false)));
        return attributesWithoutMdsDetails;
    }

    private static Optional<Address> extractCurrentAddress(List<Address> addresses) {
        Predicate<Address> addressPredicate = (Address candidateValue) -> candidateValue.getTo() == null || !candidateValue.getTo().isPresent();
        List<Address> currentValues = ImmutableList.copyOf(addresses.stream()
                .filter(addressPredicate)
                .collect(toList()));

        if (currentValues.size() > 1) {
            throw new IllegalStateException("There cannot be multiple current values for attribute.");
        }
        if (currentValues.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(currentValues.get(0));
    }

    private static <T> Optional<SimpleMdsValue<T>> getCurrentValue(final List<SimpleMdsValue<T>> simpleMdsValues) {
        Predicate<SimpleMdsValue<T>> simpleMdsValuePredicate = (SimpleMdsValue<T> simpleMdsValue) -> simpleMdsValue.getTo() == null;
        List<SimpleMdsValue<T>> currentValues = ImmutableList.copyOf(simpleMdsValues.stream()
                .filter(simpleMdsValuePredicate)
                .collect(toList()));
        if (currentValues.size() > 1) {
            throw new IllegalStateException("There cannot be multiple current values for attribute.");
        }
        if (currentValues.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(currentValues.get(0));
    }
}

