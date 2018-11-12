package com.earnix.webk.script.whatwg_dom;

import com.earnix.webk.script.web_idl.Constructor;
import com.earnix.webk.script.web_idl.DOMString;
import com.earnix.webk.script.web_idl.Exposed;
import com.earnix.webk.script.web_idl.Optional;

/**
 * @author Taras Maslov
 * 6/21/2018
 */
@Constructor
@Exposed(Window.class)
public interface Comment extends CharacterData {
    void constructor(@Optional @DOMString String data);
}
