package uk.gov.ida.verifymatchingservicetesttool.utils;

public enum ExitStatus {

    SUCCESS(0), FAILURE(1), ERROR(2);

    private final int value;

    ExitStatus(int value) {
        this.value = value;
    }

    public int getExitCode() {
        return value;
    }
}
