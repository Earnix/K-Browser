package com.earnix.webk.script.ui_events;

import com.earnix.webk.script.web_idl.Dictionary;

/**
 * @author Taras Maslov
 * 10/30/2018
 */
@Dictionary
public class EventModifierInit extends UIEventInit {

    boolean ctrlKey = false;
    
    boolean shiftKey = false;

    boolean altKey = false;

    boolean metaKey = false;

    boolean modifierAltGraph = false;

    boolean modifierCapsLock = false;

    boolean modifierFn = false;

    boolean modifierFnLock = false;

    boolean modifierHyper = false;

    boolean modifierNumLock = false;

    boolean modifierScrollLock = false;

    boolean modifierSuper = false;

    boolean modifierSymbol = false;

    boolean modifierSymbolLock = false;
}
