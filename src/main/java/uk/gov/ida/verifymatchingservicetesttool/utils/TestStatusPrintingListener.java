package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import static org.junit.platform.engine.TestExecutionResult.Status;
import static uk.gov.ida.verifymatchingservicetesttool.utils.Color.NONE;

public class TestStatusPrintingListener implements TestExecutionListener {

    private static final String RUNNING_TEST = "Running test : ";
    private static final String STATUS = "Status : ";

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
        System.out.println("Test execution finished. Number of all tests : " + tests);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            System.out.println(RUNNING_TEST + testIdentifier.getDisplayName());
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            Status status = testExecutionResult.getStatus();
            Color color = Color.valueOf(status);
            printMessage(color, STATUS + status);
            printFailureMessage(testIdentifier, testExecutionResult, color);
            System.out.println();
        }
    }

    private void printFailureMessage(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult, Color color) {
        if (testExecutionResult.getStatus().equals(Status.FAILED)) {
            testIdentifier.getSource().ifPresent(source -> printMessage(color, source.toString()));
            testExecutionResult.getThrowable()
                    .ifPresent(throwable -> printMessage(color, throwable.toString()));
        }
    }

    private void printMessage(Color color, String message) {
        System.out.println(color + message + NONE);
    }
}
