package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

public class EidasAddressDto {
    private final String poBox;
    private final String locatorDesignator;
    private final String locatorName;
    private final String cvAddressArea;
    private final String thoroughFare;
    private final String postName;
    private final String adminunitFirstLine;
    private final String adminunitSecondLine;
    private final String postCode;

    public EidasAddressDto(String poBox, String locatorDesignator, String locatorName, String cvAddressArea, String throroughFare, String postName, String adminunitFirstLine, String adminunitSecondLine, String postCode) {
        this.poBox = poBox;
        this.locatorDesignator = locatorDesignator;
        this.locatorName = locatorName;
        this.cvAddressArea = cvAddressArea;
        this.thoroughFare = throroughFare;
        this.postName = postName;
        this.adminunitFirstLine = adminunitFirstLine;
        this.adminunitSecondLine = adminunitSecondLine;
        this.postCode = postCode;
    }

    public String getPoBox() {
        return poBox;
    }

    public String getLocatorDesignator() {
        return locatorDesignator;
    }

    public String getLocatorName() {
        return locatorName;
    }

    public String getCvAddressArea() {
        return cvAddressArea;
    }

    public String getThoroughFare() {
        return thoroughFare;
    }

    public String getPostName() {
        return postName;
    }

    public String getAdminunitFirstLine() {
        return adminunitFirstLine;
    }

    public String getAdminunitSecondLine() {
        return adminunitSecondLine;
    }

    public String getPostCode() {
        return postCode;
    }
}