package com.earnix.webk.runtime.html;

import com.earnix.webk.runtime.dom.HTMLCollection;
import com.earnix.webk.runtime.web_idl.Attribute;
import com.earnix.webk.runtime.web_idl.ReadonlyAttribute;
import com.earnix.webk.runtime.web_idl.SameObject;

public interface HTMLSelectElement extends HTMLElement
{

	Attribute<Long> selectedIndex();

	Attribute<String> value();

	@SameObject @ReadonlyAttribute HTMLCollection selectedOptions();

	@SameObject @ReadonlyAttribute HTMLCollection options();

	boolean multiple();
}
