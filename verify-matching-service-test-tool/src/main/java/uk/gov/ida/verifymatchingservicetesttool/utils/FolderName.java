package uk.gov.ida.verifymatchingservicetesttool.utils;

public enum FolderName {

    MATCH_FOLDER_NAME("match"),
    NO_MATCH_FOLDER_NAME("no-match");

    private String value;

    FolderName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
