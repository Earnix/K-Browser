package org.xhtmlrenderer.js.html5.canvas;

import org.xhtmlrenderer.js.web_idl.DOMString;
import org.xhtmlrenderer.js.web_idl.DefaultString;
import org.xhtmlrenderer.js.web_idl.Dictionary;
import org.xhtmlrenderer.js.web_idl.Unrestricted;

/**
 * @author Taras Maslov
 * 6/21/2018
 */
@Dictionary
public class ImageEncodeOptions {
    @DefaultString("image/png") DOMString type;// = "image/png";
    @Unrestricted double quality = 1.0;
}
