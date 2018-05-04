package uk.gov.ida.verifymatchingservicetesttool.checkers;

import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;

public class AccountCreationChecker extends ConfigChecker {

    public AccountCreationChecker() {
        super(
                "LMS does not support account creation so we cannot run this test",
                "LMS supports account creation so we can run this test"
        );
    }

    @Override
    protected boolean isEnabled(ApplicationConfiguration configuration) {
        return configuration.getLocalMatchingServiceAccountCreationUrl() != null;
    }
}
