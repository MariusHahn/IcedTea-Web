/* NetworkSettingsPanel.java -- Sets proxy settings for network.
Copyright (C) 2010 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.sourceforge.jnlp.controlpanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.sourceforge.jnlp.runtime.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * This is the pane used with creating a JDialog version. This allows changing
 * the network configuration: Proxy
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
@SuppressWarnings("serial")
public class NetworkSettingsPanel extends JPanel implements ActionListener {

    private DeploymentConfiguration config;

    private JPanel description;
    private ArrayList<JPanel> proxyPanels = new ArrayList<JPanel>(); // The stuff with editable fields

    /** List of properties used by this panel */
    public static String[] properties = { "deployment.proxy.type",
            "deployment.proxy.http.host",
            "deployment.proxy.http.port",
            "deployment.proxy.bypass.local",
            "deployment.proxy.auto.config.url", };

    /**
     * Creates a new instance of the network settings panel.
     * 
     * @param config
     *            Loaded DeploymentConfiguration file.
     */
    public NetworkSettingsPanel(DeploymentConfiguration config) {
        super();
        this.config = config;
        setLayout(new BorderLayout());

        addComponents();
    }

    /**
     * This adds the components to the panel.
     */
    protected void addComponents() {
        JPanel settingPanel = new NamedBorderPanel(Translator.R("CPHeadNetworkSettings"));
        settingPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;

        JLabel networkDesc = new JLabel("<html>" + Translator.R("CPNetworkSettingsDescription") + "<hr /></html>");

        JLabel[] description = { new JLabel("<html>" + Translator.R("NSDescription-1") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription0") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription1") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription2") + "</html>"),
                new JLabel("<html>" + Translator.R("NSDescription3") + "</html>") };

        this.description = new JPanel(new CardLayout());
        for (int i = 0; i < description.length; i++)
            this.description.add(description[i], String.valueOf(i - 1));

        // Settings for selecting Proxy Server
        JPanel proxyServerPanel = new JPanel(new BorderLayout());
        JPanel proxyLocationPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JPanel proxyBypassPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        JLabel addressLabel = new JLabel(Translator.R("NSAddress") + ":");
        JLabel portLabel = new JLabel(Translator.R("NSPort") + ":");
        final JTextField addressField = new JTextField(config.getProperty(properties[1]), 10);
        addressField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                config.setProperty(properties[1], addressField.getText());
            }
        });

        addressField.addMouseListener(new MiddleClickListener(this.config, properties[1]));
        final JTextField portField = new JTextField(config.getProperty(properties[2]), 3);
        portField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                config.setProperty(properties[2], portField.getText());
            }
        });
        // Create the button which allows setting of other types of proxy.
        JButton advancedProxyButton = new JButton(Translator.R("NSAdvanced") + "...");
        advancedProxyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    AdvancedProxySettingsDialog.showAdvancedProxySettingsDialog(config);
                    addressField.setText(config.getProperty(properties[1]));
                    portField.setText(config.getProperty(properties[2]));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JCheckBox bypassCheckBox = new JCheckBox(Translator.R("NSBypassLocal"), Boolean.parseBoolean(config.getProperty(properties[3])));
        bypassCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                config.setProperty(properties[3], String.valueOf(e.getStateChange() == ItemEvent.SELECTED));
            }
        });
        proxyLocationPanel.add(Box.createRigidArea(new Dimension(13, 0)));
        proxyLocationPanel.add(addressLabel);
        proxyLocationPanel.add(addressField);
        proxyLocationPanel.add(portLabel);
        proxyLocationPanel.add(portField);
        proxyLocationPanel.add(advancedProxyButton);
        proxyBypassPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        proxyBypassPanel.add(bypassCheckBox);

        proxyServerPanel.add(proxyLocationPanel, BorderLayout.CENTER);
        proxyServerPanel.add(proxyBypassPanel, BorderLayout.SOUTH);

        JRadioButton directConnection = new JRadioButton(Translator.R("NSDirectConnection"), config.getProperty(properties[0]).equals("0"));
        directConnection.setActionCommand("0");
        directConnection.addActionListener(this);

        JRadioButton useProxyServer = new JRadioButton(Translator.R("NSManualProxy"), config.getProperty(properties[0]).equals("1"));
        useProxyServer.setActionCommand("1");
        useProxyServer.addActionListener(this);

        JRadioButton useAutoProxyConfigScript = new JRadioButton(Translator.R("NSAutoProxy"), config.getProperty(properties[0]).equals("2"));
        useAutoProxyConfigScript.setActionCommand("2");
        useAutoProxyConfigScript.addActionListener(this);

        JRadioButton useBrowserSettings = new JRadioButton(Translator.R("NSBrowserProxy"), config.getProperty(properties[0]).equals("3"));
        useBrowserSettings.setActionCommand("3");
        useBrowserSettings.addActionListener(this);

        ButtonGroup modeSelect = new ButtonGroup();
        modeSelect.add(useBrowserSettings);
        modeSelect.add(useProxyServer);
        modeSelect.add(useAutoProxyConfigScript);
        modeSelect.add(directConnection);

        // Settings for Automatic Proxy Configuration Script
        JPanel proxyAutoPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JLabel locationLabel = new JLabel(Translator.R("NSScriptLocation") + ":");
        final JTextField locationField = new JTextField(config.getProperty(properties[4]), 20);
        locationField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String value = locationField.getText();
                if (value.trim().equals(""))
                    value = null;
                config.setProperty(properties[4], value);
            }
        });

        proxyAutoPanel.add(Box.createRigidArea(new Dimension(13, 0)));
        proxyAutoPanel.add(locationLabel);
        proxyAutoPanel.add(locationField);

        c.gridy = 0;
        settingPanel.add(networkDesc, c);
        c.gridy = 1;
        settingPanel.add(this.description, c);
        c.gridy = 2;
        settingPanel.add(directConnection, c);
        c.gridy = 3;
        settingPanel.add(useBrowserSettings, c);
        c.gridy = 4;
        settingPanel.add(useProxyServer, c);
        c.gridy = 5;
        settingPanel.add(proxyServerPanel, c);
        proxyPanels.add(proxyServerPanel);
        c.gridy = 6;
        settingPanel.add(useAutoProxyConfigScript, c);
        c.gridy = 7;
        settingPanel.add(proxyAutoPanel, c);
        proxyPanels.add(proxyAutoPanel);

        // Filler to pack the bottom of the panel.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty = 1;
        settingPanel.add(filler, c);

        setState(); // depending on default setting we will enable or disable

        add(settingPanel, BorderLayout.CENTER);

    }

    /**
     * Enable/Disable the panel and all its children recursively.
     * 
     * @param panel
     *            JPanel which needs to be enabled or disabled.
     * @param enable
     *            true if the panel and its children are to be enabled, false
     *            otherwise.
     */
    private void enablePanel(JPanel panel, boolean enable) {
        // This will be used to enable all components in this panel recursively.
        // Ridiculously slow if lots of nested panels.
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                enablePanel((JPanel) c, enable);
            }
            c.setEnabled(enable);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        config.setProperty(properties[0], e.getActionCommand());
        setState();
    }

    /**
     * This enables and disables the appropriate panels.
     */
    private void setState() {
        ((CardLayout) this.description.getLayout()).show(this.description, config.getProperty(properties[0]));
        if (config.getProperty(properties[0]).equals("0")) {
            for (JPanel panel : proxyPanels)
                enablePanel(panel, false);
        } else if (config.getProperty(properties[0]).equals("1")) {
            enablePanel(proxyPanels.get(1), false);
            enablePanel(proxyPanels.get(0), true);
        } else if (config.getProperty(properties[0]).equals("2")) {
            enablePanel(proxyPanels.get(0), false);
            enablePanel(proxyPanels.get(1), true);
        } else if (config.getProperty(properties[0]).equals("3")) {
            for (JPanel panel : proxyPanels)
                enablePanel(panel, false);
        }
    }
}