package com.earnix.webk.tools;

import com.earnix.webk.extend.FSImage;
import com.earnix.webk.extend.ReplacedElement;
import com.earnix.webk.extend.UserAgentCallback;
import com.earnix.webk.layout.LayoutContext;
import com.earnix.webk.render.BlockBox;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.swing.AWTFSImage;
import com.earnix.webk.swing.EmptyReplacedElement;
import com.earnix.webk.swing.ImageReplacedElement;
import com.earnix.webk.util.ImageUtil;
import com.earnix.webk.util.XRLog;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author patrick
 */
public class SwingImageReplacer extends ElementReplacer {

    private final Map imageComponents;

    public SwingImageReplacer() {
        imageComponents = new HashMap();
    }

    public boolean isElementNameMatch() {
        return true;
    }

    public String getElementNameMatch() {
        return "img";
    }

    public boolean accept(final LayoutContext context, final ElementImpl element) {
        return context.getNamespaceHandler().isImageElement(element);
    }

    public ReplacedElement replace(LayoutContext context, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
        return replaceImage(uac, context, box.getElement(), cssWidth, cssHeight);
    }

    public void clear(ElementImpl element) {
        System.out.println("*** cleared image components for element " + element);
        imageComponents.remove(element);
    }

    public void reset() {
        System.out.println("*** cleared image componentes");
        imageComponents.clear();
    }

    /**
     * Handles replacement of image elements in the document. May return the same ReplacedElement for a given image
     * on multiple calls. Image will be automatically scaled to cssWidth and cssHeight assuming these are non-zero
     * positive values. The element is assume to have a src attribute (e.g. it's an <img> element)
     *
     * @param uac       Used to retrieve images on demand from some source.
     * @param context
     * @param elem      The element with the image reference
     * @param cssWidth  Target width of the image
     * @param cssHeight Target height of the image @return A ReplacedElement for the image; will not be null.
     */
    protected ReplacedElement replaceImage(UserAgentCallback uac, LayoutContext context, ElementImpl elem, int cssWidth, int cssHeight) {
        ReplacedElement re = null;

        // lookup in cache, or instantiate
        re = lookupImageReplacedElement(elem);
        if (re == null) {
            Image im = null;
            String imageSrc = context.getNamespaceHandler().getImageSourceURI(elem);
            if (imageSrc == null || imageSrc.length() == 0) {
                XRLog.layout(Level.WARNING, "No source provided for img element.");
                re = newIrreplaceableImageElement(cssWidth, cssHeight);
            } else {
                //FSImage is here since we need to capture a target H/W
                //for the image (as opposed to what the actual image size is).
                FSImage fsImage = uac.getImageResource(imageSrc).getImage();
                if (fsImage != null) {
                    im = ((AWTFSImage) fsImage).getImage();
                }

                if (im != null) {
                    re = new ImageReplacedElement(im, cssWidth, cssHeight);
                } else {
                    // TODO: Should return "broken" image icon, e.g. "not found"
                    re = newIrreplaceableImageElement(cssWidth, cssHeight);
                }
            }
            storeImageReplacedElement(elem, re);
        }
        return re;
    }

    /**
     * Adds a ReplacedElement containing an image to a cache of images for quick lookup.
     *
     * @param e  The element under which the image is keyed.
     * @param cc The replaced element containing the image, or another ReplacedElement to be used in its place
     *           (like a placeholder if the image can't be loaded).
     */
    protected void storeImageReplacedElement(ElementImpl e, ReplacedElement cc) {
        System.out.println("\n*** Cached image for element");
        imageComponents.put(e, cc);
    }

    /**
     * Retrieves a ReplacedElement for an image from cache, or null if not found.
     *
     * @param e The element by which the image is keyed
     * @return The ReplacedElement for the image, or null if there is none.
     */
    protected ReplacedElement lookupImageReplacedElement(ElementImpl e) {
        if (imageComponents.size() == 0) {
            return null;
        }
        ReplacedElement replacedElement = (ReplacedElement) imageComponents.get(e);
        return replacedElement;
    }

    /**
     * Returns a ReplacedElement for some element in the stream which should be replaceable, but is not. This might
     * be the case for an element like img, where the source isn't provided.
     *
     * @param cssWidth  Target width for the element.
     * @param cssHeight Target height for the element
     * @return A ReplacedElement to substitute for one that can't be generated.
     */
    protected ReplacedElement newIrreplaceableImageElement(int cssWidth, int cssHeight) {
        BufferedImage missingImage = null;
        ReplacedElement mre;
        try {
            // TODO: we can come up with something better; not sure if we should use Alt text, how text should size, etc.
            missingImage = ImageUtil.createCompatibleBufferedImage(cssWidth, cssHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = missingImage.createGraphics();
            g.setColor(Color.BLACK);
            g.setBackground(Color.WHITE);
            g.setFont(new Font("Serif", Font.PLAIN, 12));
            g.drawString("Missing", 0, 12);
            g.dispose();
            mre = new ImageReplacedElement(missingImage, cssWidth, cssHeight);
        } catch (Exception e) {
            mre = new EmptyReplacedElement(
                    cssWidth < 0 ? 0 : cssWidth,
                    cssHeight < 0 ? 0 : cssHeight);
        }
        return mre;
    }

}
