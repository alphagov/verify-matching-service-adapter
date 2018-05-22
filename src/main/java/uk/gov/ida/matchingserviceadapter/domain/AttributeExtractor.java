package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import java.util.Optional;

public interface AttributeExtractor {
    Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset);
}
