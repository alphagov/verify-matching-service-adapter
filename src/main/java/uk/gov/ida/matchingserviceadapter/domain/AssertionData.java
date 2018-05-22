package uk.gov.ida.matchingserviceadapter.domain;

import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import java.util.Optional;

public class AssertionData {
    private String matchingDatasetIssuer;
    private AuthnContext levelOfAssurance;
    private Optional<Cycle3Dataset> cycle3Data;
    private MatchingDataset matchingDataset;

    public AssertionData(String matchingDatasetIssuer, AuthnContext levelOfAssurance, Optional<Cycle3Dataset> cycle3Data, MatchingDataset matchingDataset) {
        this.matchingDatasetIssuer = matchingDatasetIssuer;
        this.levelOfAssurance = levelOfAssurance;
        this.cycle3Data = cycle3Data;
        this.matchingDataset = matchingDataset;
    }

    public MatchingDataset getMatchingDataset() {
        return matchingDataset;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getMatchingDatasetIssuer() {
        return matchingDatasetIssuer;
    }

    public Optional<Cycle3Dataset> getCycle3Data() {
        return cycle3Data;
    }
}
