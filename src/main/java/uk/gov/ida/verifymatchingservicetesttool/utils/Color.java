package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.junit.platform.engine.TestExecutionResult.Status;

public enum Color {

    NONE(0),
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33);

    private final String colorCode;

    Color(int colorCode) {
        this.colorCode = "\u001B[" + colorCode + "m";
    }

    static Color valueOf(Status status) {
        switch (status) {
            case SUCCESSFUL:
                return GREEN;
            case ABORTED:
                return YELLOW;
            case FAILED:
                return RED;
            default:
                return BLACK;
        }
    }

    @Override
    public String toString() {
        return this.colorCode;
    }
}
