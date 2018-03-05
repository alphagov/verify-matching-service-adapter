package uk.gov.ida.matchingserviceadapter.mappers;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.security.verification.exceptions.CertificateChainValidationException;
import uk.gov.ida.matchingserviceadapter.exceptions.InvalidSamlMetadataException;
import uk.gov.ida.matchingserviceadapter.logging.MdcHelper;
import uk.gov.ida.matchingserviceadapter.rest.soap.SamlElementType;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.SamlOverSoapException;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.deserializers.ElementToOpenSamlXMLObjectTransformer;

import javax.inject.Inject;
import java.util.function.Function;

public class DocumentToInboundMatchingServiceRequestMapper{
    private final SoapMessageManager soapMessageManager;
    private final Function<AttributeQuery, InboundMatchingServiceRequest> attributeQueryTransformer;
    private final ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller;

    @Inject
    public DocumentToInboundMatchingServiceRequestMapper(
            SoapMessageManager soapMessageManager,
            Function<AttributeQuery, InboundMatchingServiceRequest> attributeQueryTransformer,
            ElementToOpenSamlXMLObjectTransformer<AttributeQuery> attributeQueryUnmarshaller
    ) {
        this.soapMessageManager = soapMessageManager;
        this.attributeQueryTransformer = attributeQueryTransformer;
        this.attributeQueryUnmarshaller = attributeQueryUnmarshaller;
    }

    public InboundMatchingServiceRequest getInboundMatchingServiceRequest(Document attributeQueryDocument) {
        Element unwrappedMessage = soapMessageManager.unwrapSoapMessage(attributeQueryDocument, SamlElementType.AttributeQuery);

        return getInboundMatchingServiceRequest(unwrappedMessage);
    }

    private InboundMatchingServiceRequest getInboundMatchingServiceRequest(Element unwrappedMessage) {
        AttributeQuery attributeQuery = attributeQueryUnmarshaller.apply(unwrappedMessage);
        MdcHelper.addContextToMdc(attributeQuery);
        try {
            return attributeQueryTransformer.apply(attributeQuery);
        } catch (InvalidSamlMetadataException e) {
            throw new SamlOverSoapException("Hub metadata is invalid.", e, attributeQuery.getID());
        } catch (SamlTransformationErrorException e) {
            throw new SamlOverSoapException(e.getMessage(), e, attributeQuery.getID());
        } catch (CertificateChainValidationException e) {
            throw new SamlOverSoapException("Problem validating certificates received from hub.", e, attributeQuery.getID());
        }
    }
}
