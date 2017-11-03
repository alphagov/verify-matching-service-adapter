package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;

public class TestStatisticsFormatter {

    public static String format(TestExecutionSummary summary) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Test statistics:" + lineSeparator());
        stringBuilder.append(summary.getTestsFoundCount() + " tests found" + lineSeparator());
        stringBuilder.append(summary.getTestsSucceededCount() + " tests successful" + lineSeparator());
        stringBuilder.append(summary.getTestsFailedCount() + " tests failed" + lineSeparator());
        stringBuilder.append(summary.getTestsSkippedCount() + " tests skipped" + lineSeparator());

        return stringBuilder.toString();
    }

    public static String format(List<Failure> failures) {
        return failures.stream()
            .map(TestStatisticsFormatter::getFailureInfo)
            .collect(joining(lineSeparator()));
    }

    private static String getFailureInfo(Failure failure) {
        return failure.getTestIdentifier().getDisplayName() + ": " + failure.getException().getMessage();
    }
}
