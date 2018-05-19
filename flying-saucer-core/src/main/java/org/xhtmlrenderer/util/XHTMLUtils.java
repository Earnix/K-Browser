package org.xhtmlrenderer.util;

import java.util.Objects;
import java.util.OptionalInt;

import org.w3c.dom.Element;

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
public class XHTMLUtils
{

	/**
	 * Returns value of XHTML boolean attribute (e.g. multiple="multiple" ).
	 */
	public static boolean isTrue(Element el, String attr)
	{
		Objects.requireNonNull(el);
		Objects.requireNonNull(attr);

		String attValue = el.getAttribute(attr);
		return attValue != null && attValue.equalsIgnoreCase(attr);
	}

	public static int getIntValue(Element el, String attr, int defaultValue)
	{
		Objects.requireNonNull(el);
		Objects.requireNonNull(attr);

		int val = 0;
		if (el.hasAttribute(attr))
		{
			val = GeneralUtil.parseIntRelaxed(el.getAttribute(attr));
		}
		return val > 0 ? val : defaultValue;
	}

	public static OptionalInt getOptionalIntValue(Element el, String attr)
	{
		Objects.requireNonNull(el);
		Objects.requireNonNull(attr);

		int val = 0;
		if (el.hasAttribute(attr))
		{
			val = GeneralUtil.parseIntRelaxed(el.getAttribute(attr));
		}
		return val > 0 ? OptionalInt.of(val) : OptionalInt.empty();
	}

}
