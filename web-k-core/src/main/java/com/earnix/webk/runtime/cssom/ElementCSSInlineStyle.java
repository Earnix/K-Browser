package com.earnix.webk.runtime.cssom;

import com.earnix.webk.runtime.web_idl.Mixin;
import com.earnix.webk.runtime.web_idl.PutForwards;
import com.earnix.webk.runtime.web_idl.ReadonlyAttribute;
import com.earnix.webk.runtime.web_idl.SameObject;

/**
 * @author Taras Maslov
 * 11/12/2018
 */
@Mixin
public interface ElementCSSInlineStyle {
    
    @SameObject
    @PutForwards("cssText")
    @ReadonlyAttribute
    CSSStyleDeclaration style();
}
