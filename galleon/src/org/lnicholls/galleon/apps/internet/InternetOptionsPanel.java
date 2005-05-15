package org.lnicholls.galleon.apps.internet;

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

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppConfiguration;
import org.lnicholls.galleon.app.AppConfigurationPanel;
import org.lnicholls.galleon.gui.OptionsTable;
import org.lnicholls.galleon.util.NameValue;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class InternetOptionsPanel extends AppConfigurationPanel {
    private static Logger log = Logger.getLogger(InternetOptionsPanel.class.getName());

    public InternetOptionsPanel(AppConfiguration appConfiguration) {
        super(appConfiguration);
        setLayout(new GridLayout(0, 1));

        InternetConfiguration internetConfiguration = (InternetConfiguration) appConfiguration;

        mTitleField = new JTextField(internetConfiguration.getName());
        mReloadCombo = new JComboBox();
        mReloadCombo.addItem(new ComboWrapper("5 minutes", "5"));
        mReloadCombo.addItem(new ComboWrapper("10 minutes", "10"));
        mReloadCombo.addItem(new ComboWrapper("20 minutes", "20"));
        mReloadCombo.addItem(new ComboWrapper("30 minutes", "30"));
        mReloadCombo.addItem(new ComboWrapper("1 hour", "60"));
        mReloadCombo.addItem(new ComboWrapper("2 hours", "120"));
        mReloadCombo.addItem(new ComboWrapper("4 hours", "240"));
        mReloadCombo.addItem(new ComboWrapper("6 hours", "720"));
        mReloadCombo.addItem(new ComboWrapper("24 hours", "1440"));
        defaultCombo(mReloadCombo, Integer.toString(internetConfiguration.getReload()));
        mNameField = new JTextField("");
        mUrlField = new JTextField("");

        FormLayout layout = new FormLayout("right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, " + "9dlu, " + "pref, "
                + // title
                "3dlu, " + "pref, " + // reload
                "9dlu, " + "pref, " + // directories
                "9dlu, " + "pref, " + // name
                "3dlu, " + "pref, " + // url
                "3dlu, " + "pref");

        PanelBuilder builder = new PanelBuilder(layout);
        //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 4));
        builder.addLabel("Title", cc.xy(1, 3));
        builder.add(mTitleField, cc.xyw(3, 3, 1));
        builder.addLabel("Reload", cc.xy(1, 5));
        builder.add(mReloadCombo, cc.xyw(3, 5, 1));
        builder.addSeparator("URLs", cc.xyw(1, 7, 4));

        builder.addLabel("Name", cc.xy(1, 9));
        builder.add(mNameField, cc.xyw(3, 9, 1));
        builder.addLabel("URL", cc.xy(1, 11));
        builder.add(mUrlField, cc.xyw(3, 11, 1));

        mColumnValues = new ArrayList();
        int counter = 0;
        for (Iterator i = internetConfiguration.getUrls().iterator(); i.hasNext();) {
            NameValue value = (NameValue) i.next();
            ArrayList values = new ArrayList();
            values.add(0, value.getName());
            values.add(1, value.getValue());
            mColumnValues.add(counter++, values);
        }

        ArrayList columnNames = new ArrayList();
        columnNames.add(0, "Name");
        columnNames.add(1, "URL");
        ArrayList fields = new ArrayList();
        fields.add(mNameField);
        fields.add(mUrlField);
        mOptionsTable = new OptionsTable(this, columnNames, mColumnValues, fields);
        builder.add(mOptionsTable, cc.xyw(1, 13, 4));

        JPanel panel = builder.getPanel();
        //FormDebugUtils.dumpAll(panel);
        add(panel);
    }

    public void load() {
    }

    public void save() {
        log.debug("save()");
        InternetConfiguration internetConfiguration = (InternetConfiguration) mAppConfiguration;
        internetConfiguration.setName(mTitleField.getText());
        internetConfiguration.setReload(Integer.parseInt(((NameValue) mReloadCombo.getSelectedItem()).getValue()));
        ArrayList newItems = new ArrayList();
        Iterator iterator = mColumnValues.iterator();
        while (iterator.hasNext()) {
            ArrayList rows = (ArrayList) iterator.next();
            log.debug("Url=" + rows.get(0));
            newItems.add(new NameValue((String) rows.get(0), (String) rows.get(1)));
        }
        internetConfiguration.setUrls(newItems);
    }

    private JTextComponent mTitleField;

    private JComboBox mReloadCombo;

    private JTextComponent mNameField;

    private JTextComponent mUrlField;

    private OptionsTable mOptionsTable;

    private ArrayList mColumnValues;
}