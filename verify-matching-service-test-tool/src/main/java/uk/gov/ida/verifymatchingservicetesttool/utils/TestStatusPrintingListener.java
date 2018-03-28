package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.TestExecutionResult.Status;
import static uk.gov.ida.verifymatchingservicetesttool.utils.Color.NONE;

public class TestStatusPrintingListener extends SummaryGeneratingListener {

    private static final String RUNNING_TEST = "Running test : ";
    private static final String STATUS = "Status : ";

    private long totalTests = 0;

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        super.testPlanExecutionFinished(testPlan);
        long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
        System.out.println("Test execution finished. Number of all tests: " + tests);

        totalTests += tests;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        super.executionStarted(testIdentifier);
        if (testIdentifier.isTest()) {
            System.out.println(RUNNING_TEST + testIdentifier.getDisplayName());
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        super.executionFinished(testIdentifier, testExecutionResult);
        if (testIdentifier.isTest()) {
            Status status = testExecutionResult.getStatus();
            Color color = Color.valueOf(status);
            printMessage(color, STATUS + status);
            printFailureMessage(testIdentifier, testExecutionResult, color);
            System.out.println();
        }
    }

    public long getTotalTests() {
        return totalTests;
    }

    private void printFailureMessage(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult, Color color) {
        if (testExecutionResult.getStatus().equals(Status.FAILED)) {
            testExecutionResult.getThrowable()
                .ifPresent(throwable -> printMessage(color, throwable.toString()));
        }
    }

    private void printMessage(Color color, String message) {
        System.out.println(color + message + NONE);
    }
}
