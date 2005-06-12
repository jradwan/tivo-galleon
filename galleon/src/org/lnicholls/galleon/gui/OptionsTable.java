package org.lnicholls.galleon.gui;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.app.*;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JTable used in GUI
 */
public class OptionsTable extends JPanel implements ActionListener, KeyListener, ItemListener {
    private static Logger log = Logger.getLogger(OptionsTable.class.getName());

    public OptionsTable(AppConfigurationPanel optionsPanel, ArrayList columnNames, ArrayList columnValues, ArrayList fields) {
        super();

        mFields = fields;

        setLayout(new GridLayout(0, 1));

        mTableModel = new OptionsTableModel(columnNames, columnValues);

        FormLayout layout = new FormLayout("left:pref", "pref, 3dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);

        CellConstraints cc = new CellConstraints();

        JButton[] array = new JButton[3];
        mAddButton = new JButton("Add");
        array[0] = mAddButton;
        array[0].setActionCommand("add");
        array[0].addActionListener(this);
        array[0].setEnabled(false);
        mModifyButton = new JButton("Modify");
        array[1] = mModifyButton;
        array[1].setActionCommand("modify");
        array[1].addActionListener(this);
        array[1].setEnabled(false);
        mDeleteButton = new JButton("Delete");
        array[2] = mDeleteButton;
        array[2].setActionCommand("delete");
        array[2].addActionListener(this);
        array[2].setEnabled(false);
        JPanel buttons = ButtonBarFactory.buildLeftAlignedBar(array);
        builder.add(buttons, cc.xyw(1, 1, 1));

        for (int i = 0; i < mFields.size(); i++) {
            if (mFields.get(i) instanceof JTextField) {
                JTextField field = (JTextField) mFields.get(i);
                field.addKeyListener(this);
            } else if (mFields.get(i) instanceof JComboBox) {
                JComboBox combo = (JComboBox) mFields.get(i);
                combo.addItemListener(this);
            }
        }

        mTable = new JTable(mTableModel);
        mTable.setDragEnabled(true);
        mTable.setTransferHandler(optionsPanel.getTransferHandler());
        mTable.setPreferredScrollableViewportSize(new Dimension(400, 100));

        mTable.setRowSelectionAllowed(true);
        mTable.setColumnSelectionAllowed(false);
        ListSelectionModel rowSM = mTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    if (!mUpdating)
                    {
                        int selectedRow = lsm.getMinSelectionIndex();
                        for (int i = 0; i < mFields.size(); i++) {
                            if (mFields.get(i) instanceof JTextField) {
                                JTextField field = (JTextField) mFields.get(i);
                                field.setText((String) mTableModel.getValueAt(selectedRow, i, false));
                            } else if (mFields.get(i) instanceof JComboBox) {
                                JComboBox combo = (JComboBox) mFields.get(i);
                                if (combo.getSelectedItem() instanceof AppConfigurationPanel.ComboWrapper) {
                                    AppConfigurationPanel.ComboWrapper wrapper = (AppConfigurationPanel.ComboWrapper) combo.getSelectedItem();
                                    String value = (String) mTableModel.getValueAt(selectedRow, i, false);
                                    for (int j = 0; j < combo.getItemCount(); j++) {
                                        if (((NameValue) combo.getItemAt(j)).getValue().equals(value)) {
                                            combo.setSelectedIndex(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    mDeleteButton.setEnabled(true);
                    mAddButton.setEnabled(true);
                    mModifyButton.setEnabled(true);
                } else {
                    if (!mUpdating)
                    {
                        for (int i = 0; i < mFields.size(); i++) {
                            if (mFields.get(i) instanceof JTextField) {
                                JTextField field = (JTextField) mFields.get(i);
                                log.debug("adding: " + field.getText());
                                field.setText("");
                            } else if (mFields.get(i) instanceof JComboBox) {
                                JComboBox combo = (JComboBox) mFields.get(i);
                                if (combo.getSelectedItem() instanceof AppConfigurationPanel.ComboWrapper) {
                                    combo.setSelectedIndex(0);
                                }
                            }
                        }
                    }

                    mDeleteButton.setEnabled(false);
                    mAddButton.setEnabled(false);
                    mModifyButton.setEnabled(false);
                }
            }
        });
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(mTable);
        builder.add(scrollPane, cc.xyw(1, 3, 1));

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);
        checkButtonStates();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        checkButtonStates();
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            checkButtonStates();
        } else {
        }
    }

    public void checkButtonStates() {
        boolean filled = true;
        for (int i = 0; i < mFields.size(); i++) {
            if (mFields.get(i) instanceof JTextField) {
                JTextField field = (JTextField) mFields.get(i);
                if (field.getText().trim().length() == 0) {
                    filled = false;
                    break;
                }
            }
        }

        if (filled) {
            mAddButton.setEnabled(true);
            if (mTable.getSelectedRowCount() > 0)
                mModifyButton.setEnabled(true);
        } else {
            mAddButton.setEnabled(false);
            mModifyButton.setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if ("add".equals(e.getActionCommand())) {
            mUpdating = true;
            try {
                int nextRow = mTableModel.getRowCount();
                for (int i = 0; i < mFields.size(); i++) {
                    if (mFields.get(i) instanceof JTextField) {
                        JTextField field = (JTextField) mFields.get(i);
                        mTableModel.setValueAt(field.getText(), nextRow, i);
                        field.setText("");
                    } else if (mFields.get(i) instanceof JComboBox) {
                        JComboBox combo = (JComboBox) mFields.get(i);
                        if (combo.getSelectedItem() instanceof AppConfigurationPanel.ComboWrapper) {
                            AppConfigurationPanel.ComboWrapper wrapper = (AppConfigurationPanel.ComboWrapper) combo.getSelectedItem();
                            mTableModel.setValueAt(wrapper.getValue(), nextRow, i);
                        }
                    }
                }
            } catch (Exception ex) {
                Tools.logException(OptionsTable.class, ex);
            }
            mUpdating = false;
        } else if ("modify".equals(e.getActionCommand())) {
            mUpdating = true;
            try {
                int selectedRow = mTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Why do I need to do this...?
                    String[] values = new String[mFields.size()];
                    for (int i = 0; i < mFields.size(); i++) {
                        if (mFields.get(i) instanceof JTextField) {
                            JTextField field = (JTextField) mFields.get(i);
                            values[i] = field.getText();
                            //field.setText("");
                        } else if (mFields.get(i) instanceof JComboBox) {
                            JComboBox combo = (JComboBox) mFields.get(i);
                            if (combo.getSelectedItem() instanceof AppConfigurationPanel.ComboWrapper) {
                                AppConfigurationPanel.ComboWrapper wrapper = (AppConfigurationPanel.ComboWrapper) combo.getSelectedItem();
                                values[i] = wrapper.getValue();
                            }
                        }

                    }

                    for (int i = 0; i < values.length; i++) {
                        mTableModel.setValueAt(values[i], selectedRow, i);
                    }
                    mTable.addRowSelectionInterval(selectedRow, selectedRow);
                }
            } catch (Exception ex) {
                Tools.logException(OptionsTable.class, ex);
            }
            mUpdating = false;
        } else if ("delete".equals(e.getActionCommand())) {
            mUpdating = true;
            try {
                int[] selectedRows = mTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    for (int i = 0; i < selectedRows.length; i++) {
                        mTableModel.removeRow(selectedRows[i]);
                    }
                }
                for (int i = 0; i < mFields.size(); i++) {
                    JTextField field = (JTextField) mFields.get(i);
                    field.setText("");
                }
            } catch (Exception ex) {
                Tools.logException(OptionsTable.class, ex);
            }
            mUpdating = false;
        }
    }

    public OptionsTableModel getModel() {
        return mTableModel;
    }

    private ArrayList mFields;

    private OptionsTableModel mTableModel;

    private JTable mTable;

    private JButton mAddButton;

    private JButton mModifyButton;

    private JButton mDeleteButton;
    
    private boolean mUpdating;
}