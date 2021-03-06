package com.earnix.webk.runtime.html.canvas;

import com.earnix.webk.runtime.web_idl.Mixin;
import com.earnix.webk.runtime.web_idl.Unrestricted;

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
