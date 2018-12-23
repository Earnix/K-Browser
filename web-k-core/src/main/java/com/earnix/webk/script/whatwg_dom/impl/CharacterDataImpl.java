package com.earnix.webk.script.whatwg_dom.impl;

import com.earnix.webk.dom.nodes.CDataNodeModel;
import com.earnix.webk.script.ScriptContext;
import com.earnix.webk.script.impl.ChildNodeImpl;
import com.earnix.webk.script.impl.NodeImpl;
import com.earnix.webk.script.impl.NonDocumentTypeChildNodeImpl;
import com.earnix.webk.script.web_idl.Attribute;
import com.earnix.webk.script.web_idl.DOMException;
import com.earnix.webk.script.web_idl.DOMString;
import com.earnix.webk.script.whatwg_dom.CharacterData;
import com.earnix.webk.script.whatwg_dom.Element;
import com.earnix.webk.script.whatwg_dom.NonDocumentTypeChildNode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * @author Taras Maslov
 * 7/13/2018
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CharacterDataImpl extends NodeImpl implements CharacterData {

    CDataNodeModel target;
    ChildNodeImpl childNodeMixin;
    NonDocumentTypeChildNode nonDocumentTypeChildNodeMixin;

    Attribute<String> dataAttribute = Attribute.<String>receive(val -> target.text(val)).give(() -> target.getWholeText());

    public CharacterDataImpl(ScriptContext scriptContext, CDataNodeModel target) {
        super(scriptContext, target);
        this.target = target;
        childNodeMixin = new ChildNodeImpl(target);
        nonDocumentTypeChildNodeMixin = new NonDocumentTypeChildNodeImpl(scriptContext, target);
    }


    @Override
    public Attribute<String> data() {
        return dataAttribute;
    }

    @Override
    public long length() {
        return target.getWholeText().length();
    }

    @Override
    public String substringData(int offset, int count) {
        try {
            return target.getWholeText().substring(offset, offset + count + 1);
        } catch (StringIndexOutOfBoundsException e) {
            throw new DOMException("RangeError");
        }
    }

    @Override
    public void appendData(String data) {
        target.text(target.getWholeText() + data);
    }

    @Override
    public void insertData(int offset, String data) {
        StringBuilder stringBuilder = new StringBuilder(target.getWholeText());
        try {
            stringBuilder.insert(offset, data);
            target.text(stringBuilder.toString());
        } catch (StringIndexOutOfBoundsException e) {
            throw new DOMException("RangeError");
        }
    }

    @Override
    public void deleteData(int offset, int count) {
        val stringBuilder = new StringBuilder(target.getWholeText());
        try {
            stringBuilder.delete(offset, offset + count + 1);
            target.text(stringBuilder.toString());
        } catch (StringIndexOutOfBoundsException e) {
            throw new DOMException("RangeError");
        }
    }

    @Override
    public void replaceData(int offset, int count, String data) {
        val stringBuilder = new StringBuilder(target.getWholeText());
        try {
            stringBuilder.replace(offset, offset + count + 1, data);
            target.text(stringBuilder.toString());
        } catch (StringIndexOutOfBoundsException e) {
            throw new DOMException("RangeError");
        }
    }

    // region ChildNode

    @Override
    public void before(Object... nodes) {
        childNodeMixin.before(nodes);
    }

    @Override
    public void after(Object... nodes) {
        childNodeMixin.after(nodes);
    }

    @Override
    public void replaceWith(Object... nodes) {
        childNodeMixin.replaceWith(nodes);
    }

    @Override
    public void remove() {
        childNodeMixin.remove();
    }

    // endregion

    // region NonDocumentTypeChildNode

    @Override
    public Element previousElementSibling() {
        return nonDocumentTypeChildNodeMixin.previousElementSibling();
    }

    @Override
    public Element nextElementSibling() {
        return nonDocumentTypeChildNodeMixin.nextElementSibling();
    }

    // endregion


    @Override
    public @DOMString Attribute<String> textContent() {
        return new Attribute<String>() {
            @Override
            public String get() {
                return target.getWholeText();
            }

            @Override
            public void set(String s) {
                if (s != null) {
                    target.text(s);
                } else {
                    target.text("");
                }
            }
        };
    }
}
