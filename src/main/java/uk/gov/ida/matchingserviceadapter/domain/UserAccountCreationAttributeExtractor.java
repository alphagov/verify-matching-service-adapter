package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class UserAccountCreationAttributeExtractor {

    protected abstract AttributeExtractor getAttributeExtractor(String name);

    public List<Attribute> getUserAccountCreationAttributes(List<Attribute> requestedAttributes,
                                                            MatchingDataset matchingDataset,
                                                            HubAssertion cycle3Assertion) {
        if(matchingDataset == null){
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
}
