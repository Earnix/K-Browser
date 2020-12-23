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

import java.util.Optional;
import javax.swing.JComponent;

import org.apache.commons.lang3.StringUtils;
import com.earnix.webk.layout.LayoutContext;
import com.earnix.webk.render.BlockBox;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.simple.extend.XhtmlForm;

public class FileField extends InputField {

    private FormFieldState preFocusState;

    public FileField(ElementImpl e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JComponent create() {
        preFocusState = getOriginalState();
        FileInputComponent fileInputComponent = SwingComponentFactory.getInstance().createFileInputComponent(this);
        fileInputComponent.setOnChangeListener(this::valueChanged);
        return fileInputComponent;
    }

    @Override
    protected void applyOriginalState() {
        // This is always the default, since you can't set a default
        // value for this in the HTML
        FileInputComponent com = (FileInputComponent) getComponent();
        com.setFilePath(getOriginalState().getValue());
    }

    @Override
    protected String[] getFieldValues() {
        FileInputComponent com = (FileInputComponent) getComponent();
        return new String[]{com.getFilePath()};
    }

    @Override
    protected Optional<String> validateInternal() {
        if (isRequired() && StringUtils.isBlank(getFieldValues()[0])) {
            return Optional.of("File should be selected.");
        } else {
            return  super.validateInternal();
        }
    }

    protected void valueChanged(String value) {
        String fieldValue = getFieldValues()[0];
        getElement().attr("value", fieldValue);
    }
}
