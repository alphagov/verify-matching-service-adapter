package uk.gov.ida.matchingserviceadapter.rest.soap;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.exceptions.SoapUnwrappingException;
import uk.gov.ida.shared.utils.xml.XmlUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SoapMessageManagerTest {
    @Test
    public void wrapWithSoapEnvelope_shouldWrapElementInsideSoapMessageBody() throws Exception {
        Element element = getTestElement();

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        assertThat(getAttributeQuery(soapMessage)).isNotNull();
    }

    @Test
    public void unwrapSoapMessage_shouldUnwrapElementInsideSoapMessageBody() throws Exception {
        Element element = getTestElement();

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        Element unwrappedElement = manager.unwrapSoapMessage(soapMessage, SamlElementType.Response);

        assertThat(unwrappedElement).isNotNull();
        assertThat(unwrappedElement.getTagName()).isEqualTo("samlp:Response");
    }

    @Test(expected = SoapUnwrappingException.class)
    public void unwrapSoapMessage_shouldThrowExceptionIfSoapUnwrappingFails() throws Exception {
        Element element = XmlUtils.newDocumentBuilder().newDocument().createElement("foo");

        SoapMessageManager manager = new SoapMessageManager();
        Document soapMessage = manager.wrapWithSoapEnvelope(element);

        manager.unwrapSoapMessage(soapMessage, SamlElementType.Response);
    }

    private Element getTestElement() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = XmlUtils.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document.createElementNS("urn:oasis:names:tc:SAML:2.0:protocol", "samlp:Response");
    }

    private Element getAttributeQuery(Document document) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NamespaceContextImpl context = new NamespaceContextImpl();
        context.startPrefixMapping("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        context.startPrefixMapping("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        xpath.setNamespaceContext(context);

        return (Element) xpath.evaluate("//samlp:Response", document, XPathConstants.NODE);
    }
}
