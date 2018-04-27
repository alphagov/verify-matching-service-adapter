package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import java.util.Optional;

public interface AttributeExtractor {
    Optional<Attribute> transform(MatchingDataset matchingDataset, HubAssertion cycle3Assertion);
}
