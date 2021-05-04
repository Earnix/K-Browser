package com.earnix.webk.simple.extend.form;

import lombok.extern.slf4j.Slf4j;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

@Slf4j
public class DefaultSwingComponentFactory extends SwingComponentFactory {
    @Override
    public JTextField createTextField(TextField formTextField) {
        return new JTextField();
    }

    @Override
    public JButton createButton(FormField field) {
        return new JButton();
    }

    @Override
    public JTextArea createTextArea(FormField field, int rows, int cols) {
        JTextArea textArea = new JTextArea();
        if (rows > 0) {
            textArea.setRows(rows);
        }
        if (cols > 0) {
            textArea.setRows(cols);
        }
        return textArea;
    }

    @Override
    public JScrollPane createScrollPane(FormField field) {
        return new JScrollPane();
    }

    @Override
    public JCheckBox createCheckBox(FormField field) {
        return new JCheckBox();
    }

    @Override
    public JComboBox createComboBox(FormField field, List<NameValuePair> optionList) {
        ComboBoxModel model = new DefaultComboBoxModel(optionList.stream().map(kv -> kv.getName()).toArray());

        JComboBox combo = new JComboBox();
        combo.setModel(model);
        return combo;
    }

    @Override
    public JPanel createMultipleOptionsListPanel(JTable listTable, JScrollPane listScrollPane)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(listScrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override public JTable createMultipleOptionsList(FormField field, List<NameValuePair> optionList, int size)
    {
        DefaultTableModel listModel = new DefaultTableModel(optionList.size(), 2)
        {
            @Override public Class<?> getColumnClass(int columnIndex)
            {
                return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
            }

            @Override public boolean isCellEditable(int row, int column)
            {
                return column==0;
            }
        };

        for (int i = 0; i < optionList.size(); i++)
        {
            NameValuePair nameValuePair = optionList.get(i);
            listModel.setValueAt(Boolean.FALSE, i, 0);
            listModel.setValueAt(nameValuePair.getName(), i, 1);
        }

        JTable multipleSelect = new JTable(listModel);
        multipleSelect.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        multipleSelect.getColumnModel().getColumn(0).setWidth(26);
        multipleSelect.getColumnModel().getColumn(0).setMaxWidth(26);
        multipleSelect.getColumnModel().getColumn(0).setMinWidth(26);
        multipleSelect.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        multipleSelect.setTableHeader(null);

        return multipleSelect;
    }

    @Override
    public JRadioButton createRadioButton(FormField field) {
        return new JRadioButton();
    }

    @Override
    public FileInputComponent createFileInputComponent(FormField field) {
        return new FileComponentImpl();
    }

    @Override
    public void showErrorDialog(String message, JComponent parent) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.WARNING_MESSAGE);
    }
}
