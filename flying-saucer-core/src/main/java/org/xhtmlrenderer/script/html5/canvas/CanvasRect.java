package org.xhtmlrenderer.script.html5.canvas;

import org.xhtmlrenderer.script.web_idl.Mixin;
import org.xhtmlrenderer.script.web_idl.Unrestricted;

/**
 * @author Taras Maslov
 * 6/20/2018
 */
@Mixin
public interface CanvasRect {
    // rects
    void clearRect(@Unrestricted double x, @Unrestricted double y, @Unrestricted double w, @Unrestricted double h);

    void fillRect(@Unrestricted double x, @Unrestricted double y, @Unrestricted double w, @Unrestricted double h);

    void strokeRect(@Unrestricted double x, @Unrestricted double y, @Unrestricted double w, @Unrestricted double h);
}