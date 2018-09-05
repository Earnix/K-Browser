package org.xhtmlrenderer.script.impl;

import org.xhtmlrenderer.script.web_idl.DOMString;
import org.xhtmlrenderer.script.whatwg_dom.DOMImplementation;
import org.xhtmlrenderer.script.whatwg_dom.Document;
import org.xhtmlrenderer.script.whatwg_dom.DocumentType;
import org.xhtmlrenderer.script.whatwg_dom.XMLDocument;

/**
 * @author Taras Maslov
 * 7/3/2018
 */
public class DOMImplementationImpl implements DOMImplementation {
    
    @Override
    public DocumentType createDocumentType(@DOMString String qualifiedName,  @DOMString String publicId, @DOMString String systemId) {
        return null;
    }

    @Override
    public XMLDocument createDocument(@DOMString String namespace, @DOMString String qualifiedName, DocumentType doctype) {
        return null;
    }

    @Override
    public Document createHTMLDocument(@DOMString String title) {
        return null;
    }

    @Override
    public boolean hasFeature() {
        return false;
    }
    
}