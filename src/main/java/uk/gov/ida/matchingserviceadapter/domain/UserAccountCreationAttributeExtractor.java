package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserAccountCreationAttributeExtractor {

    @Inject
    public UserAccountCreationAttributeExtractor() {
        //For guice injection
    }

    public List<Attribute> getUserAccountCreationAttributes(List<Attribute> requestedAttributes,
                                                            MatchingDataset matchingDataset,
                                                            Optional<Cycle3Dataset> cycle3Assertion) {
        if (matchingDataset == null) {
            throw new IllegalArgumentException("User Account Creation requires a matching dataset.");
        }

        return requestedAttributes.stream()
                .map(Attribute::getName)
                .map(this::getAttributeExtractor)
                .map(attributeType -> attributeType.transform(matchingDataset, cycle3Assertion))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private UserAccountCreationAttribute getAttributeExtractor(String name) {
        return UserAccountCreationAttribute.getUserAccountCreationAttribute(name);
    }
}
