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

import com.earnix.webk.layout.LayoutContext;
import com.earnix.webk.render.BlockBox;
import com.earnix.webk.runtime.dom.Element;
import com.earnix.webk.runtime.dom.HTMLCollection;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.runtime.dom.impl.NodeImpl;
import com.earnix.webk.runtime.html.HTMLSelectElement;
import com.earnix.webk.runtime.web_idl.Attribute;
import com.earnix.webk.simple.extend.XhtmlForm;
import com.earnix.webk.util.GeneralUtil;
import lombok.val;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SelectField extends FormField {

    private ItemListener selectOneItemListener;
    private TableModelListener tableModelListener;
    private AdjustmentListener scrollListener;

    public SelectField(ElementImpl e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    public JComponent create() {
        List<NameValuePair> optionList = createList();
        if (shouldRenderAsList()) {
            int size = 4;
            if (hasAttribute("size")) {
                size = GeneralUtil.parseIntRelaxed(getAttribute("size"));
            }
            JTable select = SwingComponentFactory.getInstance().createMultipleOptionsList(this, optionList, size);
            select.getModel().addTableModelListener(tableModelListener = e -> tableUpdated(e));
            JScrollPane scrollPane = SwingComponentFactory.getInstance().createScrollPane(this);
            scrollPane.setViewportView(select);
            Dimension preferredSize = scrollPane.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(preferredSize.width, size * select.getRowHeight()));
            scrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener = evt -> adjustScrollPosition(evt));
            scrollPane.getHorizontalScrollBar().addAdjustmentListener(scrollListener);
            return scrollPane;
        } else {
            JComboBox comboBox = SwingComponentFactory.getInstance().createComboBox(this, optionList);
            comboBox.addItemListener(selectOneItemListener = e -> {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    singleSelectionChanged(e);
            });
            return comboBox;
        }
    }

    private void adjustScrollPosition(AdjustmentEvent evt)
    {
        Adjustable source = evt.getAdjustable();
        if (evt.getValueIsAdjusting()) {
            return;
        }
        double value = evt.getValue();
        Attribute<Double> doubleAttribute =
                source.getOrientation() == Adjustable.VERTICAL ? getElement().scrollTop() : getElement().scrollLeft();
        doubleAttribute.set(value);
    }

    private void tableUpdated(TableModelEvent e)
    {
        HTMLSelectElement selectElement = getSelectElement();
        TableModel model = (TableModel) e.getSource();
        if (e.getType()==TableModelEvent.UPDATE) {
            HTMLCollection options = selectElement.options();
            for (int index = 0; index < model.getRowCount(); index++) {
                Element item = options.item(index);
                boolean selected = Boolean.valueOf(model.getValueAt(index, 0).toString());
                boolean itemHasAttribute = item.hasAttribute("selected");

                if (selected && !itemHasAttribute) {
                    item.setAttribute("selected", "true");
                } else if (!selected && itemHasAttribute) {
                    item.removeAttribute("selected");
                }
            }
            val scriptContext = getContext().getSharedContext().getCanvas().getScriptContext();
            scriptContext.getEventManager().onchange(getElement());
        }
    }

    private void singleSelectionChanged(ItemEvent event) {
        HTMLSelectElement selectElement = getSelectElement();
        JComboBox comboBox = (JComboBox)event.getSource();
        long selectedIndex = comboBox.getSelectedIndex();
        selectElement.selectedIndex().set(selectedIndex);
        val scriptContext = getContext().getSharedContext().getCanvas().getScriptContext();
        scriptContext.getEventManager().onchange(getElement());
    }

    @Override
    protected FormFieldState loadOriginalState() {
        List<Integer> selectedIndices = new ArrayList();
        HTMLCollection options = getSelectElement().options();
        for (int i = 0; i < options.length(); i++) {
            Element option = options.item(i);
            if (option.hasAttribute("selected")) {
                selectedIndices.add(new Integer(i));
            }
        }

        Attribute<Double> scrollTop = getElement().scrollTop();
        Attribute<Double> scrollLeft = getSelectElement().scrollLeft();
        return FormFieldState.fromList(selectedIndices, scrollTop.get(), scrollLeft.get());
    }

    @Override
    protected void applyOriginalState() {
        FormFieldState originalState = getOriginalState();
        if (shouldRenderAsList()) {
            JScrollPane component = (JScrollPane) getComponent();
            component.getVerticalScrollBar().removeAdjustmentListener(scrollListener);
            component.getHorizontalScrollBar().removeAdjustmentListener(scrollListener);
            JTable table = (JTable) (component).getViewport().getView();

            TableModel model = table.getModel();
            model.removeTableModelListener(tableModelListener);
            int[] selIndices = originalState.getSelectedIndices();
            for (int index: selIndices) {
                model.setValueAt(Boolean.TRUE, index, 0);
            }

            component.getVerticalScrollBar().setValue((int)originalState.getScrollTop());
            component.getHorizontalScrollBar().setValue((int)originalState.getScrollLeft());

            component.getVerticalScrollBar().addAdjustmentListener(scrollListener);
            component.getHorizontalScrollBar().addAdjustmentListener(scrollListener);
            model.addTableModelListener(tableModelListener);
        } else {
            JComboBox select = (JComboBox) getComponent();
            select.removeItemListener(selectOneItemListener);
            // This looks strange, but basically since this is a single select, and
            // someone might have put selected="selected" on more than a single option
            // I believe that the correct play here is to select the _last_ option with
            // that attribute.
            int[] indices = originalState.getSelectedIndices();
            if (indices.length == 0) {
                select.setSelectedIndex(-1);
            } else {
                select.setSelectedIndex(indices[0]);
            }
            select.addItemListener(selectOneItemListener);
        }
    }

    protected String[] getFieldValues() {
        if (shouldRenderAsList()) {
            HTMLCollection options = getSelectElement().selectedOptions();
            List<String> submitValues = new ArrayList<>();
            for (int i = 0; i < options.length(); i++) {
                submitValues.add(options.item(i).getAttribute("value"));
            }
            return submitValues.toArray(new String[0]);
        } else {
            return new String[]{getSelectElement().value().get()};
        }
    }

    private List<NameValuePair> createList() {
        List<NameValuePair> list = new ArrayList();
        addChildren(list, getElement());
        return list;
    }

    private void addChildren(List<NameValuePair> list, ElementImpl e) {
        List<NodeImpl> children = e.getChildNodes();
        for (int i = 0; i < children.size(); i++) {
            if (!(children.get(i) instanceof ElementImpl))
                continue;
            ElementImpl child = (ElementImpl) children.get(i);

            if ("option".equals(child.nodeName())) {
                // option tag, add it
                String optionText = XhtmlForm.collectText(child);
                String optionValue = optionText;

                if (child.hasAttr("value")) {
                    optionValue = child.attr("value");
                }
                list.add(new NameValuePair(optionText, optionValue));
            } else if ("optgroup".equals(child.nodeName())) {
                // optgroup tag, append heading and indent children
                String titleText = child.attr("label");
                list.add(new NameValuePair(titleText, null));
                addChildren(list, child);
            }
        }
    }

    private boolean shouldRenderAsList() {
        return getSelectElement().multiple();
    }

    private HTMLSelectElement getSelectElement() {
        return (HTMLSelectElement) getElement();
    }

    @Override
    protected Optional<String> validateInternal() {
        if (isRequired() && shouldRenderAsList() && getFieldValues().length == 0) {
            return Optional.of("At least one option must be selected");
        }
        return Optional.empty();
    }
}
