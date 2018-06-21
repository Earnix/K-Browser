package org.xhtmlrenderer.js.html5.canvas;

import org.xhtmlrenderer.js.web_idl.Mixin;
import org.xhtmlrenderer.js.web_idl.Unrestricted;

/**
 * @author Taras Maslov
 * 6/21/2018
 */
@Mixin
public interface CanvasDrawImage {
    // drawing images
    void drawImage(CanvasImageSource image, @Unrestricted double dx, @Unrestricted double dy);

    void drawImage(CanvasImageSource image, @Unrestricted double dx, @Unrestricted double dy, @Unrestricted double dw, @Unrestricted double dh);

    void drawImage(CanvasImageSource image, @Unrestricted double sx, @Unrestricted double sy, @Unrestricted double sw, @Unrestricted double sh, @Unrestricted double dx, @Unrestricted double dy, @Unrestricted double dw, @Unrestricted double dh);
}
