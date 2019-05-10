package com.earnix.webk.runtime.dom.impl;

import com.earnix.webk.runtime.dom.HTMLCollection;
import com.earnix.webk.runtime.dom.impl.nodes.AttributesModel;
import com.earnix.webk.runtime.dom.impl.parser.Tag;
import com.earnix.webk.runtime.dom.impl.select.Elements;
import com.earnix.webk.runtime.html.HTMLSelectElement;
import com.earnix.webk.runtime.web_idl.Attribute;
import com.earnix.webk.util.XHTMLUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class HTMLSelectElementImpl extends ElementImpl implements HTMLSelectElement
{
	public HTMLSelectElementImpl(Tag tag, String baseUri, AttributesModel attributes)
	{
		super(tag, baseUri, attributes);
	}

	@Override public Attribute<Long> selectedIndex()
	{
		return new Attribute<Long>()
		{
			@Override public Long get()
			{
				Elements options = getElementsByTag("option");
				long selectedIdx = -1;
				for (int i = 0; i < options.size(); i++)
				{
					ElementImpl option = options.get(i);
					if (XHTMLUtils.isTrue(option, "selected"))
					{
						selectedIdx = i;
						break;
					}
				}
				return selectedIdx;
			}

			@Override public void set(Long aLong)
			{
				Elements options = getElementsByTag("option");
				for (int i = 0; i < options.size(); i++)
				{
					ElementImpl option = options.get(i);
					if (i == aLong)
					{
						option.setAttribute("selected", "true");
					}
					else
					{
						option.removeAttribute("selected");
					}
				}
			}
		};
	}

	@Override public Attribute<String> value()
	{
		return new Attribute<String>()
		{
			@Override public String get()
			{
				HTMLCollection htmlCollection = selectedOptions();
				return htmlCollection.length() == 0 ? "" : htmlCollection.item(0).getAttribute("value");
			}

			@Override public void set(String s)
			{
				log.warn("value set unimplemented");
			}
		};
	}

	@Override public HTMLCollection selectedOptions()
	{
		return new HTMLCollectionImpl(select("option[selected]"));
	}

	@Override public HTMLCollection options()
	{
		return new HTMLCollectionImpl(select("option"));
	}

	@Override public boolean multiple()
	{
		return hasAttribute("multiple");
	}
}
