/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.earnix.webk.simple.extend.form;

import com.earnix.webk.css.constants.CSSName;
import com.earnix.webk.css.parser.FSColor;
import com.earnix.webk.css.parser.FSRGBColor;
import com.earnix.webk.css.style.CalculatedStyle;
import com.earnix.webk.css.style.FSDerivedValue;
import com.earnix.webk.css.style.derived.LengthValue;
import com.earnix.webk.layout.LayoutContext;
import com.earnix.webk.render.BlockBox;
import com.earnix.webk.render.FSFont;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.simple.extend.URLUTF8Encoder;
import com.earnix.webk.simple.extend.XhtmlForm;
import com.earnix.webk.swing.AWTFSFont;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class FormField {

    XhtmlForm parentForm;
    ElementImpl element;
    protected FormFieldState originalState;
    JComponent component;
    LayoutContext context;
    BlockBox box;
    @Getter
    @Setter
    Supplier<String> validationProvider;

    protected Integer intrinsicWidth;
    protected Integer intrinsicHeight;


    public FormField(ElementImpl e, XhtmlForm form, LayoutContext context, BlockBox box) {
        element = e;
        parentForm = form;
        this.context = context;
        this.box = box;

        initialize();
    }

    protected ElementImpl getElement() {
        return element;
    }

    public JComponent getComponent() {
        return component;
    }

    public XhtmlForm getParentForm() {
        return parentForm;
    }

    public Dimension getIntrinsicSize() {

        int width = intrinsicWidth == null ? 0 : intrinsicWidth;
        int height = intrinsicHeight == null ? 0 : intrinsicHeight;

        return new Dimension(width, height);
    }


    public void reset() {
        applyOriginalState();
    }

    protected FormFieldState getOriginalState() {
        if (originalState == null) {
            originalState = loadOriginalState();
        }

        return originalState;
    }

    protected boolean hasAttribute(String attributeName) {
        return getElement().attr(attributeName).length() > 0;
    }

    protected String getAttribute(String attributeName) {
        return getElement().attr(attributeName);
    }

    private void initialize() {
        component = create();

        if (component != null) {

            Dimension preferredSize = component.getPreferredSize();
            if (intrinsicWidth == null)
                intrinsicWidth = preferredSize.width;
            if (intrinsicHeight == null)
                intrinsicHeight = preferredSize.height;

            component.setSize(getIntrinsicSize());
        }

        applyOriginalState();
    }

    public abstract JComponent create();

    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromString("");
    }

    protected void applyOriginalState() {
        // Do nothing
    }

    /**
     * Returns true if the value of the current FormField should be
     * sent along with the current submission.  This is used so that
     * only the value of the submit button that is used to trigger the
     * form's submission is sent.
     *
     * @param source The JComponent that caused the submission
     * @return true if it should
     */
    public boolean includeInSubmission(JComponent source) {
        return true;
    }

    // These two methods are temporary but I am using them to clean up
    // the code in XhtmlForm
    public String[] getFormDataStrings() {
        // Fields MUST have at least a name attribute to get sent.  The attr
        // can be empty, or just white space, but it must be present
        if (!hasAttribute("name")) {
            return new String[]{};
        }

        String name = getAttribute("name");
        String[] values = getFieldValues();

        for (int i = 0; i < values.length; i++) {
            values[i] = URLUTF8Encoder.encode(name) + "=" + URLUTF8Encoder.encode(values[i]);
        }

        return values;
    }

    protected abstract String[] getFieldValues();


    public BlockBox getBox() {
        return box;
    }

    public LayoutContext getContext() {
        return context;
    }

    public CalculatedStyle getStyle() {
        return getBox().getStyle();
    }

    protected void applyComponentStyle(JComponent comp) {
        Font font = getFont();
        if (font != null) {
            comp.setFont(font);
        }

        CalculatedStyle style = getStyle();

        FSColor foreground = style.getColor();
        if (foreground != null) {
            comp.setForeground(toColor(foreground));
        }

        FSColor background = style.getBackgroundColor();
        if (background != null) {
            comp.setBackground(toColor(background));
        }
    }

    private static Color toColor(FSColor color) {
        if (color instanceof FSRGBColor) {
            FSRGBColor rgb = (FSRGBColor) color;
            return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), rgb.getAlpha());
        }
        throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
    }

    public Font getFont() {
        FSFont font = getStyle().getFSFont(getContext());
        if (font instanceof AWTFSFont) {
            return ((AWTFSFont) font).getAWTFont();
        }
        return null;
    }

    protected static Integer getLengthValue(CalculatedStyle style, CSSName cssName) {
        FSDerivedValue widthValue = style.valueByName(cssName);
        if (widthValue instanceof LengthValue) {
            return (int) widthValue.asFloat();
        }

        return null;
    }

    /**
     * Non-standard attribute
     */
    public Optional<String> getDisplayName() {
        if(hasAttribute("display-name")) {
            return Optional.of(getAttribute("display-name"));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Non-standard attribute
     */
    public Optional<String> getErrorMessage() {
        if(hasAttribute("error-message")) {
            return Optional.of(getAttribute("error-message"));
        } else {
            return Optional.empty();
        }
    }

    protected String entitle(String message){
        if(getDisplayName().isPresent()) {
            return String.format("%s Field: %s.", message, getDisplayName().get());
        }
        return message;
    }
    
    /**
     * @return validation error
     */
    public final Optional<String> validate() {
        if (validationProvider != null) {
            return Optional.of(validationProvider.get());
        }
        val error = validateInternal();
        if(error.isPresent() && getErrorMessage().isPresent()){
            return getErrorMessage();
        }
        return error.map(this::entitle);
    }

    protected Optional<String> validateInternal() {
        return Optional.empty();
    }

    public boolean isRequired() {
        return element.hasAttr("required");
    }

    public void requestFocus()
    {
        getComponent().requestFocus();
    }
}
