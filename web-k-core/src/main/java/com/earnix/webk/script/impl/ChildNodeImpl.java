package com.earnix.webk.script.impl;

import com.earnix.webk.dom.nodes.NodeModel;
import com.earnix.webk.script.whatwg_dom.ChildNode;

/**
 * @author Taras Maslov
 * 7/13/2018
 */
public class ChildNodeImpl implements ChildNode {

    private NodeModel target;

    public ChildNodeImpl(NodeModel target) {
        this.target = target;
    }

    @Override
    public void before(Object... nodes) {

    }

    @Override
    public void after(Object... nodes) {

    }

    @Override
    public void replaceWith(Object... nodes) {

    }

    @Override
    public void remove() {
        target.remove();
    }
}
