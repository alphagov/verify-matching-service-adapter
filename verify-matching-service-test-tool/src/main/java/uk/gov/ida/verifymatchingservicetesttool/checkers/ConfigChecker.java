package uk.gov.ida.verifymatchingservicetesttool.checkers;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.ScenarioBase;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public abstract class ConfigChecker implements ExecutionCondition {

    private final String disableReason;
    private final String enableReason;

    protected ConfigChecker(String disableReason, String enableReason) {
        this.disableReason = disableReason;
        this.enableReason = enableReason;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return context.getTestInstance()
                .map(testInstance -> isEnabled(((ScenarioBase) testInstance).getConfiguration()))
                .map(isEnabled -> isEnabled ? enabled(enableReason) : disabled(disableReason))
                .orElse(enabled("No test instance yet leave enabled for now"));
    }

    protected abstract boolean isEnabled(ApplicationConfiguration configuration);
}
