package uk.gov.ida.matchingserviceadapter.domain;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalDate;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.Comparators;
import uk.gov.ida.matchingserviceadapter.saml.factories.UserAccountCreationAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public enum UserAccountCreationAttribute implements Serializable, AttributeExtractor {
    FIRST_NAME("firstname") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getTransliterableCurrentValue(matchingDataset.getFirstNames())
                    .map(userAccountCreationAttributeFactory::createUserAccountCreationFirstNameAttribute);
        }
    },
    FIRST_NAME_VERIFIED("firstname_verified") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getTransliterableCurrentValue(matchingDataset.getFirstNames())
                    .map(firstname ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.FIRST_NAME_VERIFIED,
                                    firstname.isVerified()
                            )
                    );
        }
    },
    MIDDLE_NAME("middlename") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getMiddleNames())
                    .map(userAccountCreationAttributeFactory::createUserAccountCreationMiddleNameAttribute);
        }
    },
    MIDDLE_NAME_VERIFIED("middlename_verified") {
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
    SURNAME("surname") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getTransliterableCurrentValue(matchingDataset.getSurnames())
                    .map(userAccountCreationAttributeFactory::createUserAccountCreationSurnameAttribute);
        }
    },
    SURNAME_VERIFIED("surname_verified") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getTransliterableCurrentValue(matchingDataset.getSurnames())
                    .map((SimpleMdsValue<String> stringSimpleMdsValue) ->
                            userAccountCreationAttributeFactory.createUserAccountCreationVerifiedAttribute(
                                    UserAccountCreationAttribute.SURNAME_VERIFIED,
                                    stringSimpleMdsValue.isVerified()
                            )
                    );
        }
    },
    DATE_OF_BIRTH("dateofbirth") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return getCurrentValue(matchingDataset.getDateOfBirths())
                    .map(userAccountCreationAttributeFactory::createUserAccountCreationDateOfBirthAttribute);
        }
    },
    DATE_OF_BIRTH_VERIFIED("dateofbirth_verified") {
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
    CURRENT_ADDRESS("currentaddress") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            return extractCurrentAddress(matchingDataset.getCurrentAddresses()).
                    map(userAccountCreationAttributeFactory::createUserAccountCreationCurrentAddressAttribute);
        }
    },
    CURRENT_ADDRESS_VERIFIED("currentaddress_verified") {
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
    ADDRESS_HISTORY("addresshistory") {
        @Override
        public Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset) {
            List<Address> allAddresses = matchingDataset.getAddresses();
            return allAddresses.isEmpty() ? Optional.empty()
                    : Optional.of(userAccountCreationAttributeFactory.createUserAccountCreationAddressHistoryAttribute(ImmutableList.copyOf(allAddresses)));
        }
    },
    CYCLE_3("cycle_3") {
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

    public String getAttributeName() {
        return attributeName;
    }

    public static UserAccountCreationAttribute getUserAccountCreationAttribute(final String name) {
        return Arrays.stream(values())
                .filter(value -> value.getAttributeName().equals(name))
                .findFirst()
                .get();
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
        return simpleMdsValues.stream().filter(simpleMdsValuePredicate).min(Comparators.comparatorByVerified());
    }

    private static Optional<SimpleMdsValue<String>> getTransliterableCurrentValue(final List<TransliterableMdsValue> simpleMdsValues) {
        return getCurrentValue(simpleMdsValues.stream().map(t -> (SimpleMdsValue<String>) t).collect(Collectors.toList()));
    }
}
