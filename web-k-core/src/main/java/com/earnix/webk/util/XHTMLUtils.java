package com.earnix.webk.util;

import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.runtime.web_idl.DOMString;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;


/*
 * {{{ header & license
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
public class XHTMLUtils {

    /**
     * Returns value of XHTML boolean attribute (e.g. multiple="multiple" ).
     * <p>
     * https://www.stevefenton.co.uk/2011/04/html-and-xhtml-boolean-attributes/
     */
    public static boolean isTrue(ElementImpl el, String attr) {
        Objects.requireNonNull(el);
        Objects.requireNonNull(attr);

        String attValue = el.attr(attr);
        return el.hasAttr(attr) && !attValue.equalsIgnoreCase("false");
    }

    public static int getIntValue(ElementImpl el, String attr, int defaultValue) {
        Objects.requireNonNull(el);
        Objects.requireNonNull(attr);

        int val = 0;
        if (el.hasAttr(attr)) {
            val = GeneralUtil.parseIntRelaxed(el.attr(attr));
        }
        return val > 0 ? val : defaultValue;
    }

    public static Double getDoubleValue(ElementImpl el, String attr, double defaultValue) {
        Objects.requireNonNull(el);
        Objects.requireNonNull(attr);
        @DOMString String value = el.attr(attr);
        Double result = defaultValue;
        try {
            if (value != null) {
                result = Double.parseDouble(value);
            }
        }
        catch (NumberFormatException ex) {
            // do nothing as default value assigned already
        }
        return result;
    }

    public static OptionalInt getOptionalIntValue(ElementImpl el, String attr) {
        Objects.requireNonNull(el);
        Objects.requireNonNull(attr);

        int val = 0;
        if (el.hasAttr(attr)) {
            val = GeneralUtil.parseIntRelaxed(el.attr(attr));
        }
        return val > 0 ? OptionalInt.of(val) : OptionalInt.empty();
    }

    public static Optional<String> getOptionalStringValue(ElementImpl el, String attr) {
        Objects.requireNonNull(el);
        Objects.requireNonNull(attr);

        String val = el.attr(attr);
        if (val != null && val.length() > 0) {
            return Optional.of(val);
        }
        return Optional.empty();
    }

}
