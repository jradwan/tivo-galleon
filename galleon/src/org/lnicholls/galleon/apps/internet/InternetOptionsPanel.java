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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
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
	private static Logger log = Logger.getLogger(InternetOptionsPanel.class
			.getName());
	public InternetOptionsPanel(AppConfiguration appConfiguration) {
		super(appConfiguration);
		setLayout(new GridLayout(0, 1));
		InternetConfiguration internetConfiguration = (InternetConfiguration) appConfiguration;
		mTitleField = new JTextField(internetConfiguration.getName());
		mSharedField = new JCheckBox("Share");
		mSharedField.setSelected(internetConfiguration.isShared());
		mSharedField.setToolTipText("Share this app");
		mSortedField = new JCheckBox("Sort");
		mSortedField.setSelected(internetConfiguration.isSorted());
		mSortedField.setToolTipText("Sort the list");
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
		defaultCombo(mReloadCombo, Integer.toString(internetConfiguration
				.getReload()));
		mNameField = new JTextField("", 255);
		mUrlField = new JTextField("", 255);
		mDescriptionField = new JTextField("", 255);
		// mTagsField = new JTextField("", 255);
		// mPrivacyCombo = new JComboBox();
		// mPrivacyCombo.addItem(new
		// ComboWrapper(InternetConfiguration.SharedUrl.PRIVATE,
		// InternetConfiguration.SharedUrl.PRIVATE));
		// mPrivacyCombo.addItem(new
		// ComboWrapper(InternetConfiguration.SharedUrl.PUBLIC,
		// InternetConfiguration.SharedUrl.PUBLIC));
		// mPrivacyCombo.addItem(new
		// ComboWrapper(InternetConfiguration.SharedUrl.FRIENDS,
		// InternetConfiguration.SharedUrl.FRIENDS));
		// defaultCombo(mPrivacyCombo, InternetConfiguration.SharedUrl.PRIVATE);
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, 50dlu:g, right:pref:grow", "pref, "
						+ "9dlu, " + "pref, "
						+ // title
						"3dlu, " + "pref, " + // share
						"3dlu, " + "pref, " + // sort
						"3dlu, " + "pref, " + // reload
						"9dlu, " + "pref, " + // directories
						"9dlu, " + "pref, " + // name
						"3dlu, " + "pref, " + // url
						"3dlu, " + "pref, " + // description
						// "3dlu, " + "pref, " + // tags
						// "3dlu, " + "pref, " + // privacy
						"3dlu, " + "pref");
		PanelBuilder builder = new PanelBuilder(layout);
		// DefaultFormBuilder builder = new DefaultFormBuilder(new
		// FormDebugPanel(), layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.addSeparator("General", cc.xyw(1, 1, 4));
		builder.addLabel("Title", cc.xy(1, 3));
		builder.add(mTitleField, cc.xyw(3, 3, 1));
		builder.add(mSharedField, cc.xyw(3, 5, 1));
		builder.add(mSortedField, cc.xyw(3, 7, 1));
		builder.addLabel("Reload", cc.xy(1, 9));
		builder.add(mReloadCombo, cc.xyw(3, 9, 1));
		builder.addSeparator("URLs", cc.xyw(1, 11, 4));
		builder.addLabel("Name", cc.xy(1, 13));
		builder.add(mNameField, cc.xyw(3, 13, 1));
		builder.addLabel("URL", cc.xy(1, 15));
		builder.add(mUrlField, cc.xyw(3, 15, 1));
		builder.addLabel("Description", cc.xy(1, 17));
		builder.add(mDescriptionField, cc.xyw(3, 17, 1));
		// builder.addLabel("Tags", cc.xy(1, 19));
		// builder.add(mTagsField, cc.xyw(3, 19, 1));
		// builder.addLabel("Privacy", cc.xy(1, 21));
		// builder.add(mPrivacyCombo, cc.xyw(3, 21, 1));
		mColumnValues = new ArrayList();
		int counter = 0;
		for (Iterator i = internetConfiguration.getSharedUrls().iterator(); i
				.hasNext();) {
			InternetConfiguration.SharedUrl value = (InternetConfiguration.SharedUrl) i
					.next();
			ArrayList values = new ArrayList();
			values.add(0, value.getName());
			values.add(1, value.getValue());
			values.add(2, value.getDescription());
			// values.add(3, value.getTags());
			// values.add(4, value.getPrivacy());
			mColumnValues.add(counter++, values);
		}
		ArrayList columnNames = new ArrayList();
		columnNames.add(0, "Name");
		columnNames.add(1, "URL");
		columnNames.add(2, "Description");
		// columnNames.add(3, "Tags");
		// columnNames.add(4, "Privacy");
		ArrayList fields = new ArrayList();
		fields.add(mNameField);
		fields.add(mUrlField);
		fields.add(mDescriptionField);
		// fields.add(mTagsField);
		// fields.add(mPrivacyCombo);
		mOptionsTable = new OptionsTable(this, columnNames, mColumnValues,
				fields);
		builder.add(mOptionsTable, cc.xyw(1, 19, 4));
		JPanel panel = builder.getPanel();
		// FormDebugUtils.dumpAll(panel);
		add(panel);
	}
	public void load() {
	}
	public boolean valid() {
		if (mTitleField.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Invalid title.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (mColumnValues.size() == 0) {
			JOptionPane.showMessageDialog(this, "No URLs configured.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	public void save() {
		InternetConfiguration internetConfiguration = (InternetConfiguration) mAppConfiguration;
		internetConfiguration.setName(mTitleField.getText());
		internetConfiguration.setReload(Integer
				.parseInt(((NameValue) mReloadCombo.getSelectedItem())
						.getValue()));
		ArrayList newItems = new ArrayList();
		Iterator iterator = mColumnValues.iterator();
		while (iterator.hasNext()) {
			ArrayList rows = (ArrayList) iterator.next();
			log.debug("Url=" + rows.get(0));
			// newItems.add(new InternetConfiguration.SharedUrl((String)
			// rows.get(0), (String) rows.get(1), (String) rows.get(2), (String)
			// rows.get(3), (String) rows.get(4)));
			newItems.add(new InternetConfiguration.SharedUrl((String) rows
					.get(0), (String) rows.get(1), (String) rows.get(2), "",
					InternetConfiguration.SharedUrl.PRIVATE));
		}
		internetConfiguration.setSharedUrls(newItems);
		internetConfiguration.setShared(mSharedField.isSelected());
	}
	private JTextComponent mTitleField;
	private JComboBox mReloadCombo;
	private JTextComponent mNameField;
	private JTextComponent mUrlField;
	private JTextComponent mDescriptionField;
	// private JTextComponent mTagsField;
	// private JComboBox mPrivacyCombo;
	private OptionsTable mOptionsTable;
	private ArrayList mColumnValues;
	private JCheckBox mSharedField;
	private JCheckBox mSortedField;
}
