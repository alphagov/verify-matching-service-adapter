package uk.gov.ida.verifymatchingservicetesttool.checkers;

import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;

public class UniversalDatasetOnlyChecker extends ConfigChecker {

    public UniversalDatasetOnlyChecker() {
        super(
                "LMS does not support universal dataset so we cannot run this test",
                "LMS supports universal dataset so we can run this test"
        );
    }

    @Override
    protected boolean isEnabled(ApplicationConfiguration configuration) {
        return configuration.getLocalMatchingServiceUsesUniversalDataSet();
    }
}
