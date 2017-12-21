package uk.gov.ida.verifymatchingservicetesttool.configurations;

public enum CommandLineOption {

    CONFIG_FILE("c"), EXAMPLES_FOLDER("e");

    private String value;

    CommandLineOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
