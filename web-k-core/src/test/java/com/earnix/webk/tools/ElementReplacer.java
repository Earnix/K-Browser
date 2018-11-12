package com.earnix.webk.tools;

import com.earnix.webk.dom.nodes.ElementModel;
import com.earnix.webk.extend.ReplacedElement;
import com.earnix.webk.extend.UserAgentCallback;
import com.earnix.webk.layout.LayoutContext;
import com.earnix.webk.render.BlockBox;

/**
 * @author patrick
 */
public abstract class ElementReplacer {
    public abstract boolean isElementNameMatch();

    public abstract String getElementNameMatch();

    public abstract boolean accept(LayoutContext context, ElementModel element);

    public abstract ReplacedElement replace(final LayoutContext context,
                                            final BlockBox box,
                                            final UserAgentCallback uac,
                                            final int cssWidth,
                                            final int cssHeight
    );

    public abstract void clear(final ElementModel element);

    public abstract void reset();
}
