package uk.gov.ida.matchingserviceadapter.builders;


import uk.gov.ida.saml.core.domain.IpAddress;

public class IpAddressBuilder {

    private String ipAddress = "1.2.3.4";

    public static IpAddressBuilder anIpAddress() {
        return new IpAddressBuilder();
    }

    public IpAddress build() {
        return new IpAddress(ipAddress);
    }

    public IpAddressBuilder withValue(String value) {
        this.ipAddress = value;
        return this;
    }
}
