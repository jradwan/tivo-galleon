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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.plaf.BorderStyle;
import com.jgoodies.plaf.HeaderStyle;

public class MainFrame extends JFrame {
    private static Logger log = Logger.getLogger(MainFrame.class.getName());

    public MainFrame(String version) {
        super("Galleon "+version);
        setDefaultCloseOperation(0);

        JMenuBar menuBar = new JMenuBar();
        menuBar.putClientProperty("jgoodies.headerStyle", HeaderStyle.BOTH);
        menuBar.putClientProperty("jgoodies.windows.borderStyle", BorderStyle.SEPARATOR);
        menuBar.putClientProperty("Plastic.borderStyle", BorderStyle.SEPARATOR);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        /*
        fileMenu.add(new MenuAction("New Plugin...", null, "", new Integer(KeyEvent.VK_N)) {

            public void actionPerformed(ActionEvent event) {
                new AddPluginDialog(JavaHMO.getMainFrame(), JavaHMO.getAppManager()).setVisible(true);
            }

        });
        */
        fileMenu.addSeparator();
        fileMenu.add(new MenuAction("Properties...", null, "", new Integer(KeyEvent.VK_P)) {

            public void actionPerformed(ActionEvent event) {
                new ServerDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
            }

        });
        fileMenu.add(new MenuAction("ToGo...", null, "", new Integer(KeyEvent.VK_T)) {

            public void actionPerformed(ActionEvent event) {
                new ToGoDialog(Galleon.getMainFrame(), Galleon.getServerConfiguration()).setVisible(true);
            }

        });        
        fileMenu.addSeparator();        
        fileMenu.add(new MenuAction("Exit", null, "", new Integer(KeyEvent.VK_X)) {

            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }

        });

        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        helpMenu.putClientProperty("jgoodies.noIcons", Boolean.TRUE);
        helpMenu.add(new MenuAction("About...", null, "", new Integer(KeyEvent.VK_A)) {

            public void actionPerformed(ActionEvent event) {
                JOptionPane
                        .showMessageDialog(
                                Galleon.getMainFrame(),
                                "Galleon Version 0.1\nhttp://galleon.sourceforge.net\njavahmo@users.sourceforge.net\n\251 2005 Leon Nicholls. All Rights Reserved.",
                                "About", JOptionPane.INFORMATION_MESSAGE);
            }

        });
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        JComponent content = createContentPane();
        setContentPane(content);

        pack();
        Dimension paneSize = getSize();
        Dimension screenSize = getToolkit().getScreenSize();
        setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);

        URL url = getClass().getClassLoader().getResource("guiicon.gif");

        ImageIcon logo = new ImageIcon(url);
        if (logo != null)
            setIconImage(logo.getImage());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
    }

    protected JComponent createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());

        //mOptionsPanelManager = new OptionsPanelManager(this);
        //mOptionsPanelManager.setMinimumSize(new Dimension(200, 100));
        //mOptionsPanelManager.setPreferredSize(new Dimension(400, 200));

        InternalFrame navigator = new InternalFrame("Plugins");
        //mPluginTree = new PluginTree(this, getPlugins());
        //navigator.setContent(createScrollPane(mPluginTree));
        navigator.setContent(createScrollPane(new JPanel()));
        navigator.setSelected(true);
        navigator.setMinimumSize(new Dimension(100, 100));
        navigator.setPreferredSize(new Dimension(150, 400));

        //JSplitPane mainSplitPane = createSplitPane(1, navigator, mOptionsPanelManager, 0.25D);
        JSplitPane mainSplitPane = createSplitPane(1, navigator, new JPanel(), 0.25D);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(mainSplitPane, "Center");

        JLabel statusField = new JLabel("\251 2004,2005 Leon Nicholls");
        statusField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusField, "West");
        panel.add(statusPanel, "South");

        panel.setPreferredSize(new Dimension(700, 420));
        return panel;
    }

    public static JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    public static JSplitPane createSplitPane(int orientation, Component comp1, Component comp2, double resizeWeight) {
        JSplitPane split = new JSplitPane(1, false, comp1, comp2);
        split.setBorder(new EmptyBorder(0, 0, 0, 0));
        split.setOneTouchExpandable(false);
        split.setResizeWeight(resizeWeight);
        return split;
    }

    /*
    public void handlePluginSelection(PluginNode pluginNode) {
        mOptionsPanelManager.setSelectedOptionsPanel(pluginNode);
    }
    */

    public DefaultTreeModel getPlugins() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
        /*
        Iterator iterator = JavaHMO.getAppManager().getPlugins();
        while (iterator.hasNext()) {
            Plugin plugin = (Plugin) iterator.next();
            root.add(new DefaultMutableTreeNode(getPluginNode(plugin)));
        }
        */
        return new DefaultTreeModel(root);
    }
/*
    private PluginNode getPluginNode(Plugin plugin) {
        PluginDescriptor pluginDescriptor = JavaHMO.getAppManager().getPluginDescriptor(plugin);
        ImageIcon icon = null;
        try {
            if (pluginDescriptor.getIcon() != null && pluginDescriptor.getIcon().length() > 0) {
                icon = new ImageIcon(plugin.getClass().getClassLoader().getResource(pluginDescriptor.getIcon()));
            }
        } catch (Exception ex) {
            HMOTools.logException(OptionsPanelManager.class, ex, "Could not load icon " + pluginDescriptor.getIcon()
                    + " for plugin " + pluginDescriptor.getClassName());
        }

        return new PluginNode(pluginDescriptor, plugin, icon);
    }

    public void addPlugin(Plugin plugin) {
        if (log.isDebugEnabled())
            log.debug("addPlugin: " + plugin);
        JavaHMO.getAppManager().addPlugin(plugin);
        mPluginTree.addPlugin(getPluginNode(plugin));
    }

    public void removePlugin(Plugin plugin) {
        if (log.isDebugEnabled())
            log.debug("removePlugin: " + plugin);
        PluginNode pluginNode = getPluginNode(plugin);
        JavaHMO.getAppManager().removePlugin(plugin);
        mPluginTree.removePlugin(pluginNode);
    }
*/
    public void refresh() {
        //mPluginTree.refresh();
    }

    class MenuAction extends AbstractAction {
        public MenuAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

/*
    public class AddPluginDialog extends JDialog implements ActionListener, ItemListener, KeyListener {

        class PluginDescriptorWrapper {
            public PluginDescriptorWrapper(PluginDescriptor pluginDescriptor) {
                mPluginDescriptor = pluginDescriptor;
            }

            public String toString() {
                return mPluginDescriptor.getName();
            }

            PluginDescriptor mPluginDescriptor;
        }

        private AddPluginDialog(JFrame frame, PluginManager pluginManager) {
            super(frame, "New Plugin", true);

            mNameField = new JTextField();
            mNameField.addKeyListener(this);
            mVersionField = new JTextField();
            mVersionField.setEditable(false);
            mReleaseDateField = new JTextField();
            mReleaseDateField.setEditable(false);
            mAuthorNameField = new JTextField();
            mAuthorNameField.setEditable(false);
            mAuthorEmailField = new JTextField();
            mAuthorEmailField.setEditable(false);
            mAuthorHomeField = new JTextField();
            mAuthorHomeField.setEditable(false);
            mDocumentationField = new JTextPane();
            mDocumentationField.setEditable(false);
            mPluginsCombo = new JComboBox();
            mPluginsCombo.addItemListener(this);

            JScrollPane paneScrollPane = new JScrollPane(mDocumentationField);
            paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            paneScrollPane.setPreferredSize(new Dimension(250, 150));
            paneScrollPane.setMinimumSize(new Dimension(10, 10));

            Iterator iterator = pluginManager.getPluginDescriptors();
            String items[] = new String[0];
            while (iterator.hasNext()) {
                PluginDescriptor pluginDescriptor = (PluginDescriptor) iterator.next();
                mPluginsCombo.addItem(new PluginDescriptorWrapper(pluginDescriptor));
            }

            getContentPane().setLayout(new BorderLayout());

            FormLayout layout = new FormLayout("right:pref, 3dlu, 150dlu:g, 3dlu, right:pref:grow", "pref, " + //name
                    "9dlu, " + "pref, " + //plugins
                    "3dlu, " + "pref, " + //type
                    "9dlu, " + "pref, " + //description
                    "3dlu, " + "pref, " + //version
                    "3dlu, " + "pref, " + //release date
                    "9dlu, " + "pref, " + //author
                    "3dlu, " + "pref, " + //name
                    "3dlu, " + "pref, " + //email
                    "3dlu, " + "pref, " //homepage
            );

            PanelBuilder builder = new PanelBuilder(layout);
            //DefaultFormBuilder builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
            builder.setDefaultDialogBorder();

            CellConstraints cc = new CellConstraints();

            builder.addLabel("Title", cc.xy(1, 1));
            builder.add(mNameField, cc.xy(3, 1));
            builder.addSeparator("Plugins", cc.xyw(1, 3, 5));
            builder.addLabel("Type", cc.xy(1, 5));
            builder.add(mPluginsCombo, cc.xy(3, 5));
            builder.addSeparator("Description", cc.xyw(1, 7, 5));
            builder.add(paneScrollPane, cc.xywh(5, 9, 1, 11, CellConstraints.RIGHT, CellConstraints.TOP));
            builder.addLabel("Version", cc.xy(1, 9));
            builder.add(mVersionField, cc.xy(3, 9));
            builder.addLabel("Release Date", cc.xy(1, 11));
            builder.add(mReleaseDateField, cc.xy(3, 11));
            builder.addSeparator("Author", cc.xyw(1, 13, 3));
            builder.addLabel("Name", cc.xy(1, 15));
            builder.add(mAuthorNameField, cc.xy(3, 15));
            builder.addLabel("Email", cc.xy(1, 17));
            builder.add(mAuthorEmailField, cc.xy(3, 17));
            builder.addLabel("Homepage", cc.xy(1, 19));
            builder.add(mAuthorHomeField, cc.xy(3, 19));

            getContentPane().add(builder.getPanel(), "Center");

            JButton[] array = new JButton[3];
            mOKButton = new JButton("OK");
            array[0] = mOKButton;
            array[0].setActionCommand("ok");
            array[0].addActionListener(this);
            array[0].setEnabled(false);
            array[1] = new JButton("Cancel");
            array[1].setActionCommand("cancel");
            array[1].addActionListener(this);
            array[2] = new JButton("Help");
            array[2].setActionCommand("help");
            array[2].addActionListener(this);
            JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

            buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            getContentPane().add(buttons, "South");
            pack();
            setLocationRelativeTo(frame);
        }

        public void actionPerformed(ActionEvent e) {
            if ("ok".equals(e.getActionCommand())) {
                PluginDescriptor pluginDescriptor = ((PluginDescriptorWrapper) mPluginsCombo.getSelectedItem()).mPluginDescriptor;
                try {
                    PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginDescriptor);
                    Class theClass = Class.forName(pluginDescriptor.getClassName(), true, pluginClassLoader);
                    Plugin plugin = (Plugin) theClass.newInstance();

                    // TODO Find better way of setting the title
                    Class[] parameters = new Class[1];
                    parameters[0] = String.class;
                    Method method = theClass.getMethod("setTitle", parameters);
                    String[] values = new String[1];
                    values[0] = mNameField.getText();
                    method.invoke(plugin, values);

                    addPlugin(plugin);
                } catch (Exception ex) {
                    HMOTools.logException(MainFrame.class, ex, "Could not add plugin : " + pluginDescriptor);
                }
            } else if ("help".equals(e.getActionCommand())) {
                try {
                    URL url = getClass().getClassLoader().getResource("newplugin.html");
                    displayHelp(url);
                } catch (Exception ex) {
                    HMOTools.logException(OptionsPanelManager.class, ex, "Could not find new plugin help ");
                }
                return;
            }
            this.setVisible(false);
        }

        public void itemStateChanged(ItemEvent e) {
            int state = e.getStateChange();
            if (state == ItemEvent.SELECTED) {
                PluginDescriptor pluginDescriptor = ((PluginDescriptorWrapper) mPluginsCombo.getSelectedItem()).mPluginDescriptor;
                mVersionField.setText(pluginDescriptor.getVersion());
                mReleaseDateField.setText(pluginDescriptor.getReleaseDate());
                mAuthorNameField.setText(pluginDescriptor.getAuthorName());
                mAuthorEmailField.setText(pluginDescriptor.getAuthorEmail());
                mAuthorHomeField.setText(pluginDescriptor.getAuthorHomepage());
                mDocumentationField.setText(pluginDescriptor.getDescription());
            } else {
                mVersionField.setText("");
                mReleaseDateField.setText("");
                mAuthorNameField.setText("");
                mAuthorEmailField.setText("");
                mAuthorHomeField.setText("");
                mDocumentationField.setText("");
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            String name = mNameField.getText();
            if (name.length() > 0) {
                Iterator iterator = JavaHMO.getAppManager().getApps();
                while (iterator.hasNext()) {
                    Plugin plugin = (Plugin) iterator.next();
                    if (plugin.getTitle().equals(name)) {
                        mOKButton.setEnabled(false);
                        return;
                    }
                }
  
                mOKButton.setEnabled(true);
                return;
            }
            mOKButton.setEnabled(false);
        }

        private JTextField mNameField;

        private JComboBox mPluginsCombo;

        private JTextField mVersionField;

        private JTextField mReleaseDateField;

        private JTextField mAuthorNameField;

        private JTextField mAuthorEmailField;

        private JTextField mAuthorHomeField;

        private JTextPane mDocumentationField;

        private JButton mOKButton;

    }
*/    

    public class ServerDialog extends JDialog implements ActionListener {

        class ReloadWrapper extends NameValue {
            public ReloadWrapper(String name, String value) {
                super(name, value);
            }

            public String toString() {
                return getName();
            }
        }

        private ServerDialog(JFrame frame, ServerConfiguration serverConfiguration) {
            super(frame, "Server Properties", true);
            mServerConfiguration = serverConfiguration;

            mNameField = new JTextField();
            mNameField.setText(serverConfiguration.getName());
            mVersionField = new JTextField();
            mVersionField.setEditable(false);
            mVersionField.setText(serverConfiguration.getVersion());
            mReloadCombo = new JComboBox();
            mReloadCombo.addItem(new ReloadWrapper("5 minutes", "5"));
            mReloadCombo.addItem(new ReloadWrapper("10 minutes", "10"));
            mReloadCombo.addItem(new ReloadWrapper("20 minutes", "20"));
            mReloadCombo.addItem(new ReloadWrapper("30 minutes", "30"));
            mReloadCombo.addItem(new ReloadWrapper("1 hour", "60"));
            mReloadCombo.addItem(new ReloadWrapper("2 hours", "120"));
            mReloadCombo.addItem(new ReloadWrapper("4 hours", "240"));
            mReloadCombo.addItem(new ReloadWrapper("6 hours", "720"));
            mReloadCombo.addItem(new ReloadWrapper("24 hours", "1440"));
            defaultCombo(mReloadCombo, Integer.toString(serverConfiguration.getReload()));
            mUseTiVoBeacon = new JCheckBox("TiVo Beacon");
            mUseTiVoBeacon.setSelected(serverConfiguration.getUseTiVoBeacon());
            mStreamingProxy = new JCheckBox("Streaming Proxy");
            mStreamingProxy.setSelected(serverConfiguration.getUseStreamingProxy());
            mGenerateThumbnails = new JCheckBox("Generate Thumbnails");
            mGenerateThumbnails.setSelected(serverConfiguration.getGenerateThumbnails());
            mShuffleItems = new JCheckBox("Shuffle Items");
            mShuffleItems.setSelected(serverConfiguration.getShuffleItems());
            mPort = new JFormattedTextField();
            try {
                MaskFormatter formatter = new MaskFormatter("####");
                mPort = new JFormattedTextField(formatter);
                mPort.setValue(new Integer(serverConfiguration.getConfiguredPort()));
            } catch (Exception ex) {
            }
            mIPAddress = new JFormattedTextField();
            mIPAddress.setText(serverConfiguration.getIPAddress());
            mNetmask = new JTextField();
            mNetmask.setText(serverConfiguration.getNetMask());
            mRecordingsPath = new JTextField();
            mRecordingsPath.setText(serverConfiguration.getRecordingsPath());
            mMediaAccessKey = new JTextField();
            mMediaAccessKey.setText(Tools.decrypt(serverConfiguration.getMediaAccessKey()));

            getContentPane().setLayout(new BorderLayout());

            FormLayout layout = new FormLayout("right:pref, 3dlu, pref, left:pref, 3dlu, right:pref:grow", "pref, " + //settings
                    "6dlu, " + "pref, " + //name
                    "3dlu, " + "pref, " + //version
                    "3dlu, " + "pref, " + //reload
                    "3dlu, " + "pref, " + //usetivobeacon, streamingproxy
                    "3dlu, " + "pref, " + //generatethumbnails, shuffleitems
                    "3dlu, " + "pref, " + //recordings path
                    "3dlu, " + "pref, " + //media access key
                    "9dlu, " + "pref, " + //network
                    "6dlu, " + "pref, " + //port
                    "3dlu, " + "pref, " + //address
                    "3dlu, " + "pref " //netmask
            );

            PanelBuilder builder = new PanelBuilder(layout);
            builder.setDefaultDialogBorder();

            CellConstraints cc = new CellConstraints();

            builder.addSeparator("Settings", cc.xyw(1, 1, 6));
            builder.addLabel("Name", cc.xy(1, 3));
            builder.add(mNameField, cc.xyw(3, 3, 2));
            builder.addLabel("Version", cc.xy(1, 5));
            builder.add(mVersionField, cc.xyw(3, 5, 2));
            builder.addLabel("Reload", cc.xy(1, 7));
            builder.add(mReloadCombo, cc.xyw(3, 7, 2));
            // TODO Only show for Windows
            builder.add(mUseTiVoBeacon, cc.xy(3, 9));
            builder.add(mStreamingProxy, cc.xy(4, 9));
            builder.add(mGenerateThumbnails, cc.xy(3, 11));
            //builder.add(mShuffleItems, cc.xy(4, 11));
            JButton button = new JButton("...");
            button.setActionCommand("pick");
            button.addActionListener(this);
            builder.addLabel("Recordings Path", cc.xy(1, 13));
            builder.add(mRecordingsPath, cc.xyw(3, 13, 2));
            builder.add(button, cc.xyw(6, 13, 1));
            builder.addLabel("Media Access Key", cc.xy(1, 15));
            builder.add(mMediaAccessKey, cc.xyw(3, 15, 2));

            builder.addSeparator("Network", cc.xyw(1, 17, 6));
            builder.addLabel("Port", cc.xy(1, 19));
            builder.add(mPort, cc.xy(3, 19));
            builder.addLabel("IP Address", cc.xy(1, 21));
            builder.add(mIPAddress, cc.xy(3, 21));
            builder.addLabel("Netmask", cc.xy(1, 23));
            builder.add(mNetmask, cc.xy(3, 23));

            getContentPane().add(builder.getPanel(), "Center");

            JButton[] array = new JButton[3];
            array[0] = new JButton("OK");
            array[0].setActionCommand("ok");
            array[0].addActionListener(this);
            array[1] = new JButton("Cancel");
            array[1].setActionCommand("cancel");
            array[1].addActionListener(this);
            array[2] = new JButton("Help");
            array[2].setActionCommand("help");
            array[2].addActionListener(this);
            JPanel buttons = ButtonBarFactory.buildCenteredBar(array);

            buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            getContentPane().add(buttons, "South");
            pack();
            setLocationRelativeTo(frame);
        }
        
        public void defaultCombo(JComboBox combo, String value) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if ("ok".equals(e.getActionCommand())) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    mServerConfiguration.setName(mNameField.getText());
                    mServerConfiguration.setReload(Integer.parseInt(((NameValue) mReloadCombo.getSelectedItem())
                            .getValue()));
                    try {
                        mServerConfiguration.setConfiguredPort(Integer.parseInt(mPort.getText()));
                    } catch (NumberFormatException ex) {
                        Tools.logException(MainFrame.class, ex, "Invalid port: " + mPort.getText());
                    }
                    mServerConfiguration.setIPAddress(mIPAddress.getText());
                    mServerConfiguration.setNetMask(mNetmask.getText());
                    mServerConfiguration.setShuffleItems(mShuffleItems.isSelected());
                    mServerConfiguration.setGenerateThumbnails(mGenerateThumbnails.isSelected());
                    mServerConfiguration.setUseStreamingProxy(mStreamingProxy.isSelected());
                    mServerConfiguration.setUseTiVoBeacon(mUseTiVoBeacon.isSelected());
                    mServerConfiguration.setRecordingsPath(mRecordingsPath.getText());
                    mServerConfiguration.setMediaAccessKey(Tools.encrypt(mMediaAccessKey.getText()));

                    Galleon.save(false);
                } catch (Exception ex) {
                    Tools.logException(MainFrame.class, ex, "Could not configure server");
                }
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else if ("help".equals(e.getActionCommand())) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    URL url = getClass().getClassLoader().getResource("server.html");
                    displayHelp(url);
                } catch (Exception ex) {
                    //Tools.logException(OptionsPanelManager.class, ex, "Could not find server help ");
                }
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return;
            }
            else
            if ("pick".equals(e.getActionCommand())) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.addChoosableFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }

                        return false;
                    }

                    //The description of this filter
                    public String getDescription() {
                        return "Directories";
                    }
                });

                int returnVal = fc.showOpenDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    mRecordingsPath.setText(file.getAbsolutePath());
                }
                this.toFront();
                return;
            }            

            this.setVisible(false);
        }

        private JTextField mNameField;

        private JTextField mVersionField;

        private JComboBox mReloadCombo;

        private JCheckBox mUseTiVoBeacon;

        private JCheckBox mStreamingProxy;

        private JCheckBox mGenerateThumbnails;

        private JCheckBox mShuffleItems;

        private JFormattedTextField mPort;

        private JTextField mIPAddress;

        private JTextField mNetmask;
        
        private JTextField mRecordingsPath;
        private JTextField mMediaAccessKey;

        private ServerConfiguration mServerConfiguration;
    }

    public void displayHelp(URL url) {
        if (mHelpDialog != null) {
            mHelpDialog.setVisible(false);
            mHelpDialog.dispose();
        }

        mHelpDialog = new HelpDialog(this, url);
        mHelpDialog.setVisible(true);
    }

    //private PluginTree mPluginTree;

    //private OptionsPanelManager mOptionsPanelManager;

    private HelpDialog mHelpDialog;
}