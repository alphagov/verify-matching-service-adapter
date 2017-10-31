package uk.gov.ida.matchingserviceadapter.rest.soap;

public enum SamlElementType {
    Response("Response"),
    AttributeQuery("AttributeQuery");

    private final String elementName;

    SamlElementType(String elementName) {

        this.elementName = elementName;
    }

    public String getElementName() {
        return elementName;
    }
}
